package com.danielculpian.cascadestudy.presentation.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielculpian.cascadestudy.data.repository.SessionRepository
import com.danielculpian.cascadestudy.domain.CascadeTimer
import com.danielculpian.cascadestudy.domain.StudySession
import com.danielculpian.cascadestudy.domain.StudySessionPreset
import com.danielculpian.cascadestudy.domain.SystemTimerClock
import com.danielculpian.cascadestudy.domain.TimerClock
import com.danielculpian.cascadestudy.domain.TimerState
import com.danielculpian.cascadestudy.domain.TimerStateMachine
import com.danielculpian.cascadestudy.domain.toStudySession
import com.danielculpian.cascadestudy.notification.NotificationHelper
import com.danielculpian.cascadestudy.receiver.AlarmScheduler
import com.danielculpian.cascadestudy.sound.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

// ViewModel managing timer UI state, coroutine ticker updates, session statistics, notifications, and audio effects
class TimerViewModel(
    private var session: StudySession = StudySessionPreset.FULL.toStudySession(),
    private val clock: TimerClock = SystemTimerClock(),
    private val sessionRepository: SessionRepository,
    private val notificationHelper: NotificationHelper? = null,
    private val soundManager: SoundManager? = null,
    private val applicationContext: Context? = null
) : ViewModel() {

    private var updateJob: Job? = null
    private var currentPreset: StudySessionPreset = StudySessionPreset.FULL
    private var previousState: TimerState? = TimerState.IDLE

    // Domain timer instance
    private var timer = CascadeTimer(
        session = session,
        stateMachine = TimerStateMachine(),
        clock = clock
    )

    // StateFlow holding the immutable UI state
    private val _uiState = MutableStateFlow(
        TimerUiState(
            state = TimerState.IDLE,
            remainingSeconds = 0L,
            currentIntervalIndex = 0,
            totalIntervals = session.intervals.size
        )
    )
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    init {
        observeStatistics()
    }

    // Subscribes to total, weekly, and daily study statistics from the repository
    private fun observeStatistics() {
        viewModelScope.launch {
            sessionRepository.totalStudySeconds.collect { totalSeconds ->
                _uiState.update { currentState ->
                    currentState.copy(
                        totalHistoricalHours = totalSeconds / 3600f
                    )
                }
            }
        }
        viewModelScope.launch {
            sessionRepository.getWeeklyStudySeconds().collect { weeklySeconds ->
                _uiState.update { currentState ->
                    currentState.copy(
                        weeklyHours = weeklySeconds / 3600f
                    )
                }
            }
        }
        viewModelScope.launch {
            sessionRepository.getDailyStudySeconds().collect { dailySeconds ->
                _uiState.update { currentState ->
                    currentState.copy(
                        todayHours = dailySeconds / 3600f
                    )
                }
            }
        }
    }

    // Updates the exposed StateFlow and handles notification/sound triggers
    private fun updateUIState() {
        val newState = timer.state
        handleNotificationsAndSounds(newState)

        _uiState.update { currentState ->
            currentState.copy(
                state = newState,
                remainingSeconds = timer.remainingSeconds,
                currentIntervalIndex = timer.currentIntervalIndex,
                totalIntervals = session.intervals.size
            )
        }
    }

    // Schedules an exact background kernel alarm for the interval completion timestamp
    private fun scheduleBackgroundAlarm() {
        applicationContext?.let { context ->
            val endTime = timer.endTimeMillis
            if (endTime != null && endTime > 0L) {
                val isLast = timer.currentIntervalIndex == session.intervals.lastIndex
                val isRest = timer.state == TimerState.RESTING
                AlarmScheduler.scheduleAlarm(
                    context = context,
                    triggerAtMillis = endTime,
                    intervalIndex = timer.currentIntervalIndex,
                    presetName = currentPreset.name,
                    isLastInterval = isLast,
                    isRest = isRest
                )
            }
        }
    }

    // Cancels any scheduled background alarm
    private fun cancelBackgroundAlarm() {
        applicationContext?.let { context ->
            AlarmScheduler.cancelAlarm(context)
        }
    }

    // Triggers relevant notification, sound effect, and immediate database save on state transition
    private fun handleNotificationsAndSounds(newState: TimerState) {
        if (newState == previousState) return

        when (newState) {
            TimerState.STUDYING -> {
                if (previousState == TimerState.RESTING || previousState == TimerState.IDLE) {
                    notificationHelper?.showIntervalStartedNotification(
                        currentInterval = timer.currentIntervalIndex + 1,
                        totalIntervals = session.intervals.size
                    )
                    soundManager?.playStartStudySound()
                }
                scheduleBackgroundAlarm()
            }
            TimerState.RESTING -> {
                if (previousState == TimerState.STUDYING) {
                    // Study interval finished: immediately persist its duration to DB
                    saveCompletedInterval(timer.currentIntervalIndex)

                    notificationHelper?.showRestStartedNotification()
                    soundManager?.playFinishStudySound()
                }
                scheduleBackgroundAlarm()
            }
            TimerState.FINISHED -> {
                if (previousState == TimerState.STUDYING || previousState == TimerState.RESTING) {
                    // If finishing directly from a study interval, persist the final study interval
                    if (previousState == TimerState.STUDYING) {
                        saveCompletedInterval(timer.currentIntervalIndex)
                    }

                    notificationHelper?.showSessionFinishedNotification()
                    soundManager?.playFinishSessionSound()
                }
                cancelBackgroundAlarm()
            }
            else -> {}
        }

        previousState = newState
    }

    // Persists a completed study interval duration to the database
    private fun saveCompletedInterval(intervalIndex: Int) {
        if (intervalIndex in session.intervals.indices) {
            val intervalSeconds = session.intervals[intervalIndex] * 60L
            viewModelScope.launch {
                sessionRepository.saveCompletedSession(
                    durationSeconds = intervalSeconds,
                    presetType = currentPreset.name
                )
            }
        }
    }

    // Selects a study session preset and updates the timer instance
    fun selectPreset(preset: StudySessionPreset) {
        stopUpdating()
        currentPreset = preset
        session = preset.toStudySession()
        previousState = TimerState.IDLE
        cancelBackgroundAlarm()
        timer = CascadeTimer(
            session = session,
            stateMachine = TimerStateMachine(),
            clock = clock
        )
        updateUIState()
    }

    // Starts the periodic coroutine ticker to update the timer every second
    private fun startUpdating() {
        updateJob?.cancel()

        updateJob = viewModelScope.launch {
            while (true) {
                update()

                if (timer.state == TimerState.FINISHED) {
                    break
                }

                delay(1.seconds)
            }
        }
    }

    // Cancels the active ticker coroutine
    private fun stopUpdating() {
        updateJob?.cancel()
        updateJob = null
    }

    // Starts the timer session and begins periodic UI updates
    fun start() {
        if (!timer.start()) return
        updateUIState()
        scheduleBackgroundAlarm()
        startUpdating()
    }

    // Pauses the timer and stops periodic updates
    fun pause() {
        if (!timer.pause()) return
        cancelBackgroundAlarm()
        updateUIState()
        stopUpdating()
    }

    // Resumes the timer and restarts periodic updates
    fun resume() {
        if (!timer.resume()) return
        updateUIState()
        scheduleBackgroundAlarm()
        startUpdating()
    }

    // Resets the timer to IDLE and stops periodic updates
    fun reset() {
        if (!timer.reset()) return
        previousState = TimerState.IDLE
        cancelBackgroundAlarm()
        updateUIState()
        stopUpdating()
    }

    // Finishes the study session early, plays completion sound, posts notification, and saves accumulated current interval study time
    fun finishSessionEarly() {
        if (timer.state == TimerState.IDLE) return

        val currentIntervalElapsedSeconds = timer.getCurrentIntervalElapsedStudySeconds()

        // Trigger session completion notification and sound
        notificationHelper?.showSessionFinishedNotification()
        soundManager?.playFinishSessionSound()

        // Save accumulated elapsed time from current active interval
        if (currentIntervalElapsedSeconds > 0) {
            viewModelScope.launch {
                sessionRepository.saveCompletedSession(
                    durationSeconds = currentIntervalElapsedSeconds,
                    presetType = currentPreset.name
                )
            }
        }

        reset()
    }

    // Manual tick update for timer state and remaining time
    fun update() {
        timer.update()
        updateUIState()
    }

    override fun onCleared() {
        soundManager?.release()
        cancelBackgroundAlarm()
    }
}
