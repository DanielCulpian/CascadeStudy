package com.example.cascadestudy.domain

class TimerStateMachine {
    // To save the actual state
    var state: TimerState = TimerState.IDLE
        private set

    // To save de previous state
    private var previousState: TimerState? = null

    // Transition IDLE to STUDYING
    private fun start(){
        if(state == TimerState.IDLE)
            state = TimerState.STUDYING
    }

    // Transition STUDYING/RESTING to PAUSED
    private fun pause(){
        if(state == TimerState.STUDYING || state == TimerState.RESTING)
            state = TimerState.PAUSED
    }

    // Transition PAUSED to STUDYING/RESTING
    private fun resume(){
        if(state == TimerState.PAUSED){
            state = previousState ?: TimerState.IDLE
            previousState = null
        }
    }

    // Transition ANY to IDLE
    private fun reset(){
        state = TimerState.IDLE
        previousState = null
    }

    // Transition STUDYING to RESTING
    private fun intervalFinished(){
        if(state == TimerState.STUDYING)
            state = TimerState.RESTING
    }

    // Transition RESTING to STUDYING
    private fun restFinished(){
        if(state == TimerState.RESTING)
            state = TimerState.STUDYING
    }

    // Public function to manage events
    fun onEvent(event: TimerEvent){
        when(event){
            TimerEvent.Start -> start()
            TimerEvent.Pause -> pause()
            TimerEvent.Resume -> resume()
            TimerEvent.Reset -> reset()
            TimerEvent.IntervalFinished -> intervalFinished()
            TimerEvent.RestFinished -> restFinished()
        }
    }
}