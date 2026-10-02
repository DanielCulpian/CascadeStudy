package com.example.cascadestudy.domain

class CascadeTimer(
    private val session: StudySession,
    private val stateMachine: TimerStateMachine,
    private val clock: TimerClock
){

    var endTimeMillis: Long? = null
        private set

    var currentIntervalIndex: Int = 0
        private set

    var remainingSeconds: Long = 0
        private set

    fun start(){
        if(!stateMachine.onEvent(TimerEvent.Start))
            return

        currentIntervalIndex = 0

        val durationMinutes = session.intervals[currentIntervalIndex]
        val durationMillis = durationMinutes * 60_000L

        remainingSeconds = durationMinutes * 60L
        endTimeMillis = clock.nowMillis() + durationMillis

    }

    fun pause(){
        if (!stateMachine.onEvent(TimerEvent.Pause))
            return

        val endTime = endTimeMillis ?: return
        val remainingMillis = endTime - clock.nowMillis()

        remainingSeconds = maxOf(0, remainingMillis/1000)

        endTimeMillis = null

    }

    fun resume(){
        if (!stateMachine.onEvent(TimerEvent.Resume))
            return

        val durationMillis = remainingSeconds * 1000L

        endTimeMillis = clock.nowMillis() + durationMillis
    }

    fun reset(){
        if(!stateMachine.onEvent(TimerEvent.Reset))
            return

        // Setting the global variables to their initial values
        currentIntervalIndex = 0
        endTimeMillis = null
        remainingSeconds = 0

    }

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

    fun restFinished(){
        if (!stateMachine.onEvent(TimerEvent.RestFinished))
            return

        currentIntervalIndex++

        val durationMinutes = session.intervals[currentIntervalIndex]
        val durationMillis = durationMinutes * 60_000L

        remainingSeconds = durationMinutes * 60L
        endTimeMillis = clock.nowMillis() + durationMillis
    }

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