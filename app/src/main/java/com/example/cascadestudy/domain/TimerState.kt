package com.example.cascadestudy.domain

// Just an enum class to set the different states
enum class TimerState {
    IDLE, // Running but doing nothing
    STUDYING, // Studying
    RESTING, // Resting
    PAUSED, // Manually stopped the countdown
    FINISHED // Not running, any interval on, application finished
}