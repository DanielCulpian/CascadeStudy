package com.danielculpian.cascadestudy.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danielculpian.cascadestudy.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeeklyHistoryViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyHistoryUiState())
    val uiState: StateFlow<WeeklyHistoryUiState> = _uiState.asStateFlow()

    init {
        observeWeeklySummaries()
    }

    private fun observeWeeklySummaries() {
        viewModelScope.launch {
            sessionRepository.getClosedWeeklySummaries().collect { summaries ->
                val years = summaries.map { it.year }.distinct().sortedDescending()
                _uiState.update { currentState ->
                    val newState = currentState.copy(
                        allSummaries = summaries,
                        availableYears = years
                    )
                    applyFilters(newState)
                }
            }
        }
    }

    fun selectYear(year: Int?) {
        _uiState.update { currentState ->
            val newState = currentState.copy(selectedYear = year)
            applyFilters(newState)
        }
    }

    fun selectMonth(month: Int?) {
        _uiState.update { currentState ->
            val newState = currentState.copy(selectedMonth = month)
            applyFilters(newState)
        }
    }

    fun setHourFilter(type: HourFilterType, threshold: Float) {
        _uiState.update { currentState ->
            val newState = currentState.copy(
                hourFilterType = type,
                filterHoursThreshold = threshold
            )
            applyFilters(newState)
        }
    }

    fun resetFilters() {
        _uiState.update { currentState ->
            val newState = currentState.copy(
                selectedYear = null,
                selectedMonth = null,
                hourFilterType = HourFilterType.ALL,
                filterHoursThreshold = 0f
            )
            applyFilters(newState)
        }
    }

    private fun applyFilters(state: WeeklyHistoryUiState): WeeklyHistoryUiState {
        var filtered = state.allSummaries

        // Filter by Year
        if (state.selectedYear != null) {
            filtered = filtered.filter { it.year == state.selectedYear }
        }

        // Filter by Month
        if (state.selectedMonth != null) {
            filtered = filtered.filter { it.month == state.selectedMonth }
        }

        // Filter by Hours
        filtered = when (state.hourFilterType) {
            HourFilterType.ALL -> filtered
            HourFilterType.MORE_THAN -> filtered.filter { it.totalHours >= state.filterHoursThreshold }
            HourFilterType.LESS_THAN -> filtered.filter { it.totalHours <= state.filterHoursThreshold }
        }

        return state.copy(filteredSummaries = filtered)
    }
}
