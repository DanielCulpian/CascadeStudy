package com.example.cascadestudy.domain

// Here the different events that the TimerStateMachine can manage
interface TimerEvent {
    data object Start : TimerEvent
    data object Pause : TimerEvent
    data object Resume : TimerEvent
    data object Reset : TimerEvent
    data object IntervalFinished : TimerEvent
    data object RestFinished : TimerEvent
}