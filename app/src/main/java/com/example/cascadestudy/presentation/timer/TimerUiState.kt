package com.example.cascadestudy.presentation.timer

import com.example.cascadestudy.domain.TimerState

// UI state representation for the timer screen including study statistics
data class TimerUiState(
    val state: TimerState,
    val remainingSeconds: Long,
    val currentIntervalIndex: Int,
    val totalIntervals: Int,
    val totalHistoricalHours: Float = 0f,
    val weeklyHours: Float = 0f
)
