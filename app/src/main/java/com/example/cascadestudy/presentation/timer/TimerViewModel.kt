package com.example.cascadestudy.presentation.timer

// Own implementation of the timer
import com.example.cascadestudy.domain.CascadeTimer
import com.example.cascadestudy.domain.StudySession
import com.example.cascadestudy.domain.SystemTimerClock
import com.example.cascadestudy.domain.TimerStateMachine
import com.example.cascadestudy.domain.TimerState
import com.example.cascadestudy.domain.TimerClock

// External imports
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.seconds

// ViewModel managing timer UI state and coroutine updates
class TimerViewModel(
    private val session: StudySession = StudySession(),
    private val clock: TimerClock = SystemTimerClock()
): ViewModel() {
    // Job to handle periodic ticker updates
    private var updateJob: Job? = null

    // Domain timer instance
    private val timer = CascadeTimer(
        session = session,
        stateMachine = TimerStateMachine(),
        clock = clock
    )

    // StateFlow holding the immutable UI state
    private val _uiState = MutableStateFlow(
        TimerUiState(
            state = TimerState.IDLE,
            remainingSeconds = 0L,
            currentIntervalIndex = 0
        )
    )
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    // Updates the exposed StateFlow with current timer values
    private fun updateUIState(){
        _uiState.value = TimerUiState(
            state = timer.state,
            remainingSeconds = timer.remainingSeconds,
            currentIntervalIndex = timer.currentIntervalIndex
        )
    }

    // Starts the periodic coroutine ticker to update the timer every second
    private fun startUpdating(){
        updateJob?.cancel()

        updateJob = viewModelScope.launch {
            while(true){
                update()

                if(timer.state == TimerState.FINISHED)
                    break

                delay(1.seconds)
            }
        }
    }

    // Cancels the active ticker coroutine
    private fun stopUpdating(){
        updateJob?.cancel()
        updateJob = null
    }

    // Starts the timer session and begins periodic UI updates
    fun start(){
        if(!timer.start())
            return

        updateUIState()
        startUpdating()
    }

    // Pauses the timer and stops periodic updates
    fun pause(){
        timer.pause()
        updateUIState()
        stopUpdating()
    }

    // Resumes the timer and restarts periodic updates
    fun resume(){
        timer.resume()
        updateUIState()
        startUpdating()
    }

    // Resets the timer to IDLE and stops periodic updates
    fun reset(){
        timer.reset()
        updateUIState()
        stopUpdating()
    }

    // Manual tick update for timer state and remaining time
    fun update(){
        timer.update()
        updateUIState()
    }
}