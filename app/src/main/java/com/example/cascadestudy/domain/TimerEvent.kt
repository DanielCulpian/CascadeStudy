package com.example.cascadestudy.domain

// Sealed interface representing events processed by the TimerStateMachine
sealed interface TimerEvent {
    data object Start : TimerEvent
    data object Pause : TimerEvent
    data object Resume : TimerEvent
    data object Reset : TimerEvent
    data class IntervalFinished(
        val isLastInterval: Boolean
    ) : TimerEvent
    data object RestFinished : TimerEvent
}
