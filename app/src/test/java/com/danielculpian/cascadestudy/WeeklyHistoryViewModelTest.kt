package com.danielculpian.cascadestudy

import com.danielculpian.cascadestudy.data.repository.SessionRepository
import com.danielculpian.cascadestudy.presentation.history.HourFilterType
import com.danielculpian.cascadestudy.presentation.history.WeeklyHistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WeeklyHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDao: FakeCompletedSessionDao
    private lateinit var repository: SessionRepository
    private lateinit var viewModel: WeeklyHistoryViewModel

    @Before
    fun setup() {
        fakeDao = FakeCompletedSessionDao()
        repository = SessionRepository(
            dao = fakeDao,
            ioDispatcher = Dispatchers.Unconfined
        )
        viewModel = WeeklyHistoryViewModel(repository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun initialState_hasEmptySummaries_whenNoSessionsRecorded() = runBlocking {
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.allSummaries.isEmpty())
        assertTrue(viewModel.uiState.value.filteredSummaries.isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun filteringByHourThreshold_filtersSummariesCorrectly() = runBlocking {
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        viewModel.setHourFilter(HourFilterType.MORE_THAN, 5f)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        assertEquals(HourFilterType.MORE_THAN, viewModel.uiState.value.hourFilterType)
        assertEquals(5f, viewModel.uiState.value.filterHoursThreshold)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun resetFilters_restoresInitialFilterState() = runBlocking {
        viewModel.selectYear(2026)
        viewModel.selectMonth(12)
        viewModel.setHourFilter(HourFilterType.MORE_THAN, 10f)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        viewModel.resetFilters()
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        assertEquals(null, viewModel.uiState.value.selectedYear)
        assertEquals(null, viewModel.uiState.value.selectedMonth)
        assertEquals(HourFilterType.ALL, viewModel.uiState.value.hourFilterType)
    }
}
