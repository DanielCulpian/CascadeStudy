package com.example.cascadestudy.domain

class CascadeTimer(
    private val session: StudySession,
    private val stateMachine: TimerStateMachine
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
        endTimeMillis = System.currentTimeMillis() + durationMillis // System.currentTimeMillis() is provisional

    }

    fun pause(){
        if (!stateMachine.onEvent(TimerEvent.Pause))
            return

        val endTime = endTimeMillis ?: return
        val remainingMillis = endTime - System.currentTimeMillis()

        remainingSeconds = maxOf(0, remainingMillis/1000)

        endTimeMillis = null

    }

    fun resume(){
        if (!stateMachine.onEvent(TimerEvent.Resume))
            return

        val durationMillis = remainingSeconds * 1000L

        endTimeMillis = System.currentTimeMillis() + durationMillis
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

        if (!stateMachine.onEvent(
                TimerEvent.IntervalFinished(isLastInterval)
        ))
            return
    }

    fun restFinished(){
        if (!stateMachine.onEvent(TimerEvent.RestFinished))
            return

        currentIntervalIndex++

        val durationMinutes = session.intervals[currentIntervalIndex]
        val durationMillis = durationMinutes * 60_000L

        remainingSeconds = durationMinutes * 60L
        endTimeMillis = System.currentTimeMillis() + durationMillis
    }

    fun update(){
        val endTime = endTimeMillis ?: return

        val remainingMillis = endTime - System.currentTimeMillis()

        remainingSeconds = maxOf(0, remainingMillis/1000)
    }
}