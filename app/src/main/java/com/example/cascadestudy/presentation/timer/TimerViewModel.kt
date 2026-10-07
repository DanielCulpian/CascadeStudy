package com.example.cascadestudy.presentation.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cascadestudy.data.repository.SessionRepository
import com.example.cascadestudy.domain.CascadeTimer
import com.example.cascadestudy.domain.StudySession
import com.example.cascadestudy.domain.StudySessionPreset
import com.example.cascadestudy.domain.SystemTimerClock
import com.example.cascadestudy.domain.TimerClock
import com.example.cascadestudy.domain.TimerState
import com.example.cascadestudy.domain.TimerStateMachine
import com.example.cascadestudy.domain.toStudySession
import com.example.cascadestudy.notification.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

// ViewModel managing timer UI state, coroutine ticker updates, session statistics, and notifications
class TimerViewModel(
    private var session: StudySession = StudySessionPreset.FULL.toStudySession(),
    private val clock: TimerClock = SystemTimerClock(),
    private val sessionRepository: SessionRepository,
    private val notificationHelper: NotificationHelper? = null
) : ViewModel() {

    private var updateJob: Job? = null
    private var currentPreset: StudySessionPreset = StudySessionPreset.FULL
    private var isSessionSaved = false
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

    // Subscribes to total and weekly study statistics from the repository
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
    }

    // Updates the exposed StateFlow and handles notification triggers on state changes
    private fun updateUIState() {
        val newState = timer.state
        handleNotifications(newState)

        _uiState.update { currentState ->
            currentState.copy(
                state = newState,
                remainingSeconds = timer.remainingSeconds,
                currentIntervalIndex = timer.currentIntervalIndex,
                totalIntervals = session.intervals.size
            )
        }
    }

    // Triggers relevant notification based on state transition
    private fun handleNotifications(newState: TimerState) {
        if (newState == previousState) return

        when (newState) {
            TimerState.STUDYING -> {
                if (previousState == TimerState.RESTING || previousState == TimerState.IDLE) {
                    notificationHelper?.showIntervalStartedNotification(
                        currentInterval = timer.currentIntervalIndex + 1,
                        totalIntervals = session.intervals.size
                    )
                }
            }
            TimerState.RESTING -> {
                if (previousState == TimerState.STUDYING) {
                    notificationHelper?.showRestStartedNotification()
                }
            }
            TimerState.FINISHED -> {
                if (previousState == TimerState.STUDYING || previousState == TimerState.RESTING) {
                    notificationHelper?.showSessionFinishedNotification()
                }
            }
            else -> {}
        }

        previousState = newState
    }

    // Selects a study session preset and updates the timer instance
    fun selectPreset(preset: StudySessionPreset) {
        stopUpdating()
        currentPreset = preset
        session = preset.toStudySession()
        isSessionSaved = false
        previousState = TimerState.IDLE
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
                    onSessionFinished()
                    break
                }

                delay(1.seconds)
            }
        }
    }

    // Persists the completed study session record to local storage
    private fun onSessionFinished() {
        if (!isSessionSaved) {
            isSessionSaved = true
            val totalStudySeconds = session.intervals.sum() * 60L
            viewModelScope.launch {
                sessionRepository.saveCompletedSession(
                    durationSeconds = totalStudySeconds,
                    presetType = currentPreset.name
                )
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
        isSessionSaved = false
        updateUIState()
        startUpdating()
    }

    // Pauses the timer and stops periodic updates
    fun pause() {
        if (!timer.pause()) return
        updateUIState()
        stopUpdating()
    }

    // Resumes the timer and restarts periodic updates
    fun resume() {
        if (!timer.resume()) return
        updateUIState()
        startUpdating()
    }

    // Resets the timer to IDLE and stops periodic updates
    fun reset() {
        if (!timer.reset()) return
        isSessionSaved = false
        previousState = TimerState.IDLE
        updateUIState()
        stopUpdating()
    }

    // Manual tick update for timer state and remaining time
    fun update() {
        timer.update()
        updateUIState()
        if (timer.state == TimerState.FINISHED) {
            onSessionFinished()
        }
    }
}
