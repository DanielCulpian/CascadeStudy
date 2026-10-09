package com.danielculpian.cascadestudy.presentation.timer

import com.danielculpian.cascadestudy.domain.TimerState

// UI state representation for the timer screen including study statistics
data class TimerUiState(
    val state: TimerState,
    val remainingSeconds: Long,
    val currentIntervalIndex: Int,
    val totalIntervals: Int,
    val totalHistoricalHours: Float = 0f,
    val weeklyHours: Float = 0f
)
