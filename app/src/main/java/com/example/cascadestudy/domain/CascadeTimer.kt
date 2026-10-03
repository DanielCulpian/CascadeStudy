package com.example.cascadestudy.domain

// Main timer controller that coordinates StudySession, TimerStateMachine, and TimerClock
class CascadeTimer(
    private val session: StudySession,
    private val stateMachine: TimerStateMachine,
    private val clock: TimerClock
){
    // Time in milliseconds when the current interval or rest will finish
    var endTimeMillis: Long? = null
        private set

    // Index of the current study interval
    var currentIntervalIndex: Int = 0
        private set

    // Remaining seconds of the current interval or rest
    var remainingSeconds: Long = 0
        private set

    // Current state of the timer
    val state: TimerState
        get() = stateMachine.state

    // Starts the study session from the first interval
    fun start(): Boolean{
        if(!stateMachine.onEvent(TimerEvent.Start))
            return false

        currentIntervalIndex = 0

        val durationMinutes = session.intervals[currentIntervalIndex]
        val durationMillis = durationMinutes * 60_000L

        remainingSeconds = durationMinutes * 60L
        endTimeMillis = clock.nowMillis() + durationMillis

        return true
    }

    // Pauses the running timer and saves the remaining seconds
    fun pause(){
        val endTime = endTimeMillis ?: return

        if (!stateMachine.onEvent(TimerEvent.Pause))
            return

        val remainingMillis = endTime - clock.nowMillis()

        remainingSeconds = maxOf(0, remainingMillis/1000)

        endTimeMillis = null

    }

    // Resumes the timer from the paused state
    fun resume(){
        if (!stateMachine.onEvent(TimerEvent.Resume))
            return

        val durationMillis = remainingSeconds * 1000L

        endTimeMillis = clock.nowMillis() + durationMillis
    }

    // Resets the timer and all counters to their initial values
    fun reset(){
        if(!stateMachine.onEvent(TimerEvent.Reset))
            return

        // Setting the global variables to their initial values
        currentIntervalIndex = 0
        endTimeMillis = null
        remainingSeconds = 0

    }

    // Handles the completion of a study interval
    fun intervalFinished(){
        val isLastInterval = currentIntervalIndex == session.intervals.lastIndex

        if (!stateMachine.onEvent(TimerEvent.IntervalFinished(isLastInterval)))
            return

        if(isLastInterval){
            endTimeMillis = null
            remainingSeconds = 0
            return
        }

        val restDurationMinutes = session.restDurationMinutes
        val restDurationMillis = restDurationMinutes * 60_000L

        remainingSeconds = restDurationMinutes * 60L
        endTimeMillis = clock.nowMillis() + restDurationMillis
    }

    // Handles the completion of a rest period and advances to the next interval
    fun restFinished(){
        if (!stateMachine.onEvent(TimerEvent.RestFinished))
            return

        currentIntervalIndex++

        val durationMinutes = session.intervals[currentIntervalIndex]
        val durationMillis = durationMinutes * 60_000L

        remainingSeconds = durationMinutes * 60L
        endTimeMillis = clock.nowMillis() + durationMillis
    }

    // Updates remaining time and triggers transitions when time reaches zero
    fun update(){
        val endTime = endTimeMillis ?: return

        val remainingMillis = endTime - clock.nowMillis()

        if(remainingMillis > 0){
            remainingSeconds = remainingMillis/1000
            return
        }

        remainingSeconds = 0

        when(stateMachine.state){
            TimerState.STUDYING -> intervalFinished()
            TimerState.RESTING -> restFinished()
            else -> {}
        }
    }
}