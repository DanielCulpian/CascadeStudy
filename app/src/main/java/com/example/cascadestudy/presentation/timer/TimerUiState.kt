package com.example.cascadestudy.presentation.timer

import com.example.cascadestudy.domain.TimerState

// UI state representation for the timer screen
data class TimerUiState(
    val state: TimerState,
    val remainingSeconds: Long,
    val currentIntervalIndex: Int
)
