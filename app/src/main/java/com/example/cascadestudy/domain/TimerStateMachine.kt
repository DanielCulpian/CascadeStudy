package com.example.cascadestudy.domain

class TimerStateMachine {
    // To save the actual state
    var state: TimerState = TimerState.IDLE
        private set

    // To save de previous state
    private var previousState: TimerState? = null

    // Transition IDLE to STUDYING
    private fun start(): Boolean{
        if(state != TimerState.IDLE)
            return false

        state = TimerState.STUDYING
        return true
    }

    // Transition STUDYING/RESTING to PAUSED
    private fun pause(): Boolean{
        if(state != TimerState.STUDYING && state != TimerState.RESTING)
            return false

        previousState = state
        state = TimerState.PAUSED

        return true
    }

    // Transition PAUSED to STUDYING/RESTING
    private fun resume(): Boolean{
        if(state != TimerState.PAUSED)
            return false

        state = previousState ?: TimerState.IDLE
        previousState = null

        return true
    }

    // Transition ANY to IDLE
    private fun reset(): Boolean{
        state = TimerState.IDLE
        previousState = null

        return true
    }

    // Transition STUDYING to RESTING
    private fun intervalFinished(isLastInterval: Boolean): Boolean{
        if(state != TimerState.STUDYING)
            return false

        state = if(isLastInterval)
            TimerState.FINISHED
        else
            TimerState.RESTING

        return true
    }

    // Transition RESTING to STUDYING
    private fun restFinished(): Boolean{
        if(state != TimerState.RESTING)
            return false

        state = TimerState.STUDYING

        return true
    }

    // Public function to manage events
    fun onEvent(event: TimerEvent): Boolean{
        return when(event){
            TimerEvent.Start -> start()
            TimerEvent.Pause -> pause()
            TimerEvent.Resume -> resume()
            TimerEvent.Reset -> reset()
            is TimerEvent.IntervalFinished -> intervalFinished(event.isLastInterval)
            TimerEvent.RestFinished -> restFinished()
        }
    }
}