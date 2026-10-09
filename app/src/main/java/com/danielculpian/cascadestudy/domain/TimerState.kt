package com.danielculpian.cascadestudy.domain

// Represents the possible operational states of the study timer
enum class TimerState {
    IDLE,     // Timer is idle and waiting to start
    STUDYING, // Actively running a study interval
    RESTING,  // Actively running a break/rest interval
    PAUSED,   // Timer is paused
    FINISHED  // Study session has ended
}
