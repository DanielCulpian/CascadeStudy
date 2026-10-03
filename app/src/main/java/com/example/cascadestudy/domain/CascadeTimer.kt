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

    // Remaining time in milliseconds for the current interval or rest
    var remainingMillis: Long = 0
        private set

    // Remaining seconds of the current interval or rest
    val remainingSeconds: Long
        get() = remainingMillis/1000

    // Current state of the timer
    val state: TimerState
        get() = stateMachine.state

    private fun startInterval(durationMillis: Long){
        remainingMillis = durationMillis
        endTimeMillis = clock.nowMillis() + durationMillis
    }

    // Starts the study session from the first interval
    fun start(): Boolean{
        if(!stateMachine.onEvent(TimerEvent.Start))
            return false

        currentIntervalIndex = 0

        val durationMinutes = session.intervals[currentIntervalIndex]
        val durationMillis = durationMinutes * 60_000L

        startInterval(durationMillis)

        return true
    }

    // Pauses the running timer and saves the remaining seconds
    fun pause(): Boolean{
        val endTime = endTimeMillis ?: return false

        if (!stateMachine.onEvent(TimerEvent.Pause))
            return false

        val remainingMillis = maxOf(0, endTime - clock.nowMillis())
        this.remainingMillis = remainingMillis

        endTimeMillis = null

        return true
    }

    // Resumes the timer from the paused state
    fun resume(): Boolean{
        if (!stateMachine.onEvent(TimerEvent.Resume))
            return false

        endTimeMillis = clock.nowMillis() + remainingMillis

        return true
    }

    // Resets the timer and all counters to their initial values
    fun reset(): Boolean{
        if(!stateMachine.onEvent(TimerEvent.Reset))
            return false

        // Setting the global variables to their initial values
        currentIntervalIndex = 0
        endTimeMillis = null
        remainingMillis = 0

        return true
    }

    // Handles the completion of a study interval
    private fun intervalFinished(){
        val isLastInterval = currentIntervalIndex == session.intervals.lastIndex

        if (!stateMachine.onEvent(TimerEvent.IntervalFinished(isLastInterval)))
            return

        if(isLastInterval){
            endTimeMillis = null
            remainingMillis = 0
            return
        }

        val restDurationMinutes = session.restDurationMinutes
        val restDurationMillis = restDurationMinutes * 60_000L

        startInterval(restDurationMillis)
    }

    // Handles the completion of a rest period and advances to the next interval
    private fun restFinished(){
        if (!stateMachine.onEvent(TimerEvent.RestFinished))
            return

        currentIntervalIndex++

        val durationMinutes = session.intervals[currentIntervalIndex]
        val durationMillis = durationMinutes * 60_000L

        startInterval(durationMillis)
    }

    // Updates remaining time and triggers transitions when time reaches zero
    fun update(){
        val endTime = endTimeMillis ?: return

        val remainingMillis = endTime - clock.nowMillis()

        if(remainingMillis > 0){
            this.remainingMillis = remainingMillis
            return
        }

        this.remainingMillis = 0

        when(stateMachine.state){
            TimerState.STUDYING -> intervalFinished()
            TimerState.RESTING -> restFinished()
            else -> {}
        }
    }
}