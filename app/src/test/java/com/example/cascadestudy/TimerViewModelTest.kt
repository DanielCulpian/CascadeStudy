package com.example.cascadestudy

import com.example.cascadestudy.data.repository.SessionRepository
import com.example.cascadestudy.domain.StudySession
import com.example.cascadestudy.domain.SystemTimerClock
import com.example.cascadestudy.domain.TimerClock
import com.example.cascadestudy.domain.TimerState
import com.example.cascadestudy.notification.NotificationHelper
import com.example.cascadestudy.presentation.timer.TimerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// Unit tests for TimerViewModel behavior, coroutines, UI state flows, and notification triggers
class TimerViewModelTest {

    // Test session with predefined intervals and rest duration
    private val testSession = StudySession(
        intervals = listOf(1, 1, 1),
        restDurationMinutes = 1
    )

    // Rule to provide a test dispatcher for coroutines
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Creates a TimerViewModel with custom session, clock, and notification helper settings
    private fun createViewModel(
        session: StudySession = testSession,
        clock: TimerClock = SystemTimerClock(),
        notificationHelper: NotificationHelper? = null
    ): TimerViewModel {
        val fakeDao = FakeCompletedSessionDao()
        val repository = SessionRepository(
            dao = fakeDao,
            ioDispatcher = Dispatchers.Unconfined
        )
        return TimerViewModel(
            session = session,
            clock = clock,
            sessionRepository = repository,
            notificationHelper = notificationHelper
        )
    }

    // Verifies starting the ViewModel updates UI state to STUDYING
    @Test
    fun start_changesUiStateToStudying() {
        // Set
        val viewModel = createViewModel()

        // Act
        viewModel.start()

        // Assert
        assertEquals(TimerState.STUDYING, viewModel.uiState.value.state)
    }

    // Verifies starting a study interval triggers the interval started notification
    @Test
    fun start_triggersIntervalStartedNotification() {
        // Set
        val fakeNotificationHelper = FakeNotificationHelper()
        val viewModel = createViewModel(notificationHelper = fakeNotificationHelper)

        // Act
        viewModel.start()

        // Asser
        assertEquals(1, fakeNotificationHelper.intervalStartedCount)
    }

    // Verifies completing a study interval triggers the rest started notification
    @Test
    fun studyIntervalFinished_triggersRestStartedNotification() {
        // Set
        val fakeClock = FakeTimerClock()
        val fakeNotificationHelper = FakeNotificationHelper()
        val viewModel = createViewModel(
            session = testSession,
            clock = fakeClock,
            notificationHelper = fakeNotificationHelper
        )

        // Act
        viewModel.start()
        fakeClock.advanceMillis(60_000L)
        viewModel.update()

        // Assert
        assertEquals(1, fakeNotificationHelper.restStartedCount)
    }

    // Verifies completing the session triggers the session finished notification
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun sessionFinished_triggersSessionFinishedNotification() {
        // Set
        val fakeClock = FakeTimerClock()
        val fakeNotificationHelper = FakeNotificationHelper()
        val viewModel = createViewModel(
            session = testSession,
            clock = fakeClock,
            notificationHelper = fakeNotificationHelper
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
        assertEquals(1, fakeNotificationHelper.sessionFinishedCount)
    }

    // Verifies resetting the ViewModel updates UI state to IDLE
    @Test
    fun reset_changesUiStateToIdle() {
        // Set
        val viewModel = createViewModel()

        // Act
        viewModel.start()
        viewModel.reset()

        // Assert
        assertEquals(TimerState.IDLE, viewModel.uiState.value.state)
    }

    // Verifies manual update recalculates remaining seconds after advancing time
    @Test
    fun update_after20Seconds_updateRemainingTime() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        viewModel.update()

        // Assert
        assertEquals(40L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies UI state changes to RESTING when a study interval finishes
    @Test
    fun update_whenStudyIntervalFinishes_changesUiStateToResting() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

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
    fun update_whenRestFinishes_changesUiStateToStudying() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

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
    fun pause_changesUiStateToPaused() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

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
    fun resume_whenPaused_continueFromRemainingTime() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

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
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun start_automaticallyUpdatesRemainingTime() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)

        // Assert
        assertEquals(40L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies pausing stops the automatic coroutine ticker updates
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pause_stopsAutomaticUpdates() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

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
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun resume_restartsAutomaticUpdates() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

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
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun update_whenLastIntervalFinishes_changesUIStateToFinished() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

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

    // Verifies UI state changes to FINISHED when the session finishes
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun sessionFinishes_stopAutomaticUpdates() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

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

        // Act
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(TimerState.FINISHED, viewModel.uiState.value.state)
    }

    // Verifies resetting stops automatic updates
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun reset_stopAutomaticUpdates() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)
        viewModel.reset()
        fakeClock.advanceMillis(10_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)

        // Assert
        assertEquals(TimerState.IDLE, viewModel.uiState.value.state)
        assertEquals(0, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies starting a ViewModel with already running session does nothing
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun start_whenAlreadyRunning_doesNothing() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)
        viewModel.start()

        // Assert
        assertEquals(TimerState.STUDYING, viewModel.uiState.value.state)
        assertEquals(40L, viewModel.uiState.value.remainingSeconds)
        assertEquals(0, viewModel.uiState.value.currentIntervalIndex)
    }

    // Verifies pausing a ViewModel with already paused session does nothing
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pause_whenAlreadyPaused_doesNothing() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)
        viewModel.pause()
        viewModel.pause()

        // Assert
        assertEquals(TimerState.PAUSED, viewModel.uiState.value.state)
        assertEquals(40L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies resuming a ViewModel with already started session does nothing
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun resume_whenNotPaused_doesNothing() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

        // Act
        viewModel.start()
        fakeClock.advanceMillis(20_000L)
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1_000L)
        viewModel.resume()

        // Assert
        assertEquals(TimerState.STUDYING, viewModel.uiState.value.state)
        assertEquals(40L, viewModel.uiState.value.remainingSeconds)
    }

    // Verifies that the current interval index is exposed
    @Test
    fun start_exposesTotalNumberOfIntervals() {
        // Set
        val fakeClock = FakeTimerClock()
        val viewModel = createViewModel(session = testSession, clock = fakeClock)

        // Act
        viewModel.start()

        // Assert
        assertEquals(3, viewModel.uiState.value.totalIntervals)
    }
}
