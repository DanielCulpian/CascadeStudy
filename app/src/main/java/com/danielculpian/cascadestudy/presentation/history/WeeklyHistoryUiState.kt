package com.danielculpian.cascadestudy.presentation.history

import com.danielculpian.cascadestudy.domain.WeeklySummary

enum class HourFilterType {
    ALL,
    MORE_THAN,
    LESS_THAN
}

data class WeeklyHistoryUiState(
    val allSummaries: List<WeeklySummary> = emptyList(),
    val filteredSummaries: List<WeeklySummary> = emptyList(),
    val selectedYear: Int? = null,
    val selectedMonth: Int? = null,
    val hourFilterType: HourFilterType = HourFilterType.ALL,
    val filterHoursThreshold: Float = 0f,
    val availableYears: List<Int> = emptyList(),
    val availableMonths: List<Int> = (1..12).toList()
)
