package com.example.cascadestudy

import com.example.cascadestudy.domain.StudySession
import com.example.cascadestudy.domain.TimerState
import com.example.cascadestudy.presentation.timer.TimerViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// Unit tests for TimerViewModel behavior, coroutines, and UI state flows
class TimerViewModelTest{

    private val testSession = StudySession(
        intervals = listOf(1, 1, 1),
        restDurationMinutes = 1
    )

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Verifies starting the ViewModel updates UI state to STUDYING
    @Test
    fun start_changesUiStateToStudying(){
        // Set
        val viewModel = TimerViewModel()

        // Act
        viewModel.start()

        // Assert
        assertEquals(TimerState.STUDYING, viewModel.uiState.value.state)
    }

    // Verifies resetting the ViewModel updates UI state to IDLE
    @Test
    fun reset_changesUiStateToIdle(){
        // Set
        val viewModel = TimerViewModel()

        // Act
        viewModel.start()
        viewModel.reset()

        // Assert
        assertEquals(TimerState.IDLE, viewModel.uiState.value.state)
    }

    // Verifies manual update recalculates remaining seconds after advancing time
    @Test
    fun update_after20Seconds_updateRemainingTime(){
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = TimerViewModel(
            session = testSession,
            clock = fakeClock
        )

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        viewModel.update()

        // Assert
        assertEquals(40L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies UI state changes to RESTING when a study interval finishes
    @Test
    fun update_whenStudyIntervalFinishes_changesUiStateToResting(){
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = TimerViewModel(
            session = testSession,
            clock = fakeClock
        )

        // Act
        viewModel.start()
        fakeClock.advanceMillis(60_000L)
        viewModel.update()

        // Assert
        assertEquals(TimerState.RESTING, viewModel.uiState.value.state)
        assertEquals(60L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies UI state changes back to STUDYING when a rest period finishes
    @Test
    fun update_whenRestFinishes_changesUiStateToStudying(){
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = TimerViewModel(
            session = testSession,
            clock = fakeClock
        )

        // Act
        viewModel.start()
        fakeClock.advanceMillis(60_000L)
        viewModel.update()
        fakeClock.advanceMillis(60_000L)
        viewModel.update()

        // Assert
        assertEquals(TimerState.STUDYING, viewModel.uiState.value.state)
        assertEquals(60L, viewModel.uiState.value.remainingSeconds)
        assertEquals(1, viewModel.uiState.value.currentIntervalIndex)
    }

    // Verifies pausing the ViewModel updates UI state to PAUSED
    @Test
    fun pause_changesUiStateToPaused(){
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = TimerViewModel(
            session = testSession,
            clock = fakeClock
        )

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        viewModel.pause()

        // Assert
        assertEquals(TimerState.PAUSED, viewModel.uiState.value.state)
        assertEquals(40L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies resuming from PAUSED continues tracking time correctly
    @Test
    fun resume_whenPaused_continueFromRemainingTime(){
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = TimerViewModel(
            session = testSession,
            clock = fakeClock
        )

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        viewModel.pause()
        viewModel.resume()
        fakeClock.advanceMillis(10_000L)
        viewModel.update()

        // Assert
        assertEquals(TimerState.STUDYING, viewModel.uiState.value.state)
        assertEquals(30L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies coroutine ticker automatically updates remaining time on coroutine dispatch
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun start_automaticallyUpdatesRemainingTime(){
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = TimerViewModel(
            session = testSession,
            clock = fakeClock
        )

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)

        // Assert
        assertEquals(40L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies pausing stops the automatic coroutine ticker updates
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun pause_stopsAutomaticUpdates(){
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = TimerViewModel(
            session = testSession,
            clock = fakeClock
        )

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)
        viewModel.pause()
        fakeClock.advanceMillis(10_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)

        // Assert
        assertEquals(TimerState.PAUSED, viewModel.uiState.value.state)
        assertEquals(40L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies resuming restarts the automatic coroutine ticker updates
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun resume_restartsAutomaticUpdates(){
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = TimerViewModel(
            session = testSession,
            clock = fakeClock
        )

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)
        viewModel.pause()
        viewModel.resume()
        fakeClock.advanceMillis(10_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)

        // Assert
        assertEquals(TimerState.STUDYING, viewModel.uiState.value.state)
        assertEquals(30L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies UI state changes to FINISHED and stops ticker when the last interval ends
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun update_whenLastIntervalFinishes_changesUIStateToFinished(){
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = TimerViewModel(
            session = testSession,
            clock = fakeClock
        )

        // Act
        viewModel.start()
        fakeClock.advanceMillis(60_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)
        fakeClock.advanceMillis(60_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)
        fakeClock.advanceMillis(60_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)
        fakeClock.advanceMillis(60_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)
        fakeClock.advanceMillis(60_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)

        // Assert
        assertEquals(TimerState.FINISHED, viewModel.uiState.value.state)
        assertEquals(0, viewModel.uiState.value.remainingSeconds)
    }
}