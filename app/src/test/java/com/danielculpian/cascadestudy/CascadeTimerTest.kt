package com.danielculpian.cascadestudy

import com.danielculpian.cascadestudy.domain.CascadeTimer
import com.danielculpian.cascadestudy.domain.StudySession
import com.danielculpian.cascadestudy.domain.TimerState
import com.danielculpian.cascadestudy.domain.TimerStateMachine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Unit tests for CascadeTimer state transitions and time tracking
class CascadeTimerTest {

    // Virtual session to use in tests
    private val testSession = StudySession(
        intervals = listOf(1, 1, 1),
        restDurationMinutes = 1
    )

    // Verifies that starting a session transitions state to STUDYING and sets initial time
    @Test
    fun start_startsStudying() {
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()

        // Assert
        assertEquals(TimerState.STUDYING, stateMachine.state)
        assertEquals(0, timer.currentIntervalIndex)
        assertEquals(60_000L, timer.endTimeMillis)
        assertEquals(60L, timer.remainingSeconds)
    }

    // Verifies getElapsedStudySeconds calculates exact accumulated study time when studying or paused
    @Test
    fun getElapsedStudySeconds_calculatesAccumulatedStudyTime() {
        val session = StudySession(
            intervals = listOf(10, 10, 10),
            restDurationMinutes = 2
        )
        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // IDLE
        assertEquals(0L, timer.getElapsedStudySeconds())

        // Start interval 0 (10 min = 600s)
        timer.start()
        fakeClock.advanceMillis(120_000L) // 2 minutes elapsed (120s)
        timer.update()

        // Should have studied 120s
        assertEquals(120L, timer.getElapsedStudySeconds())

        // Pause
        timer.pause()
        fakeClock.advanceMillis(60_000L) // 1 min paused
        timer.update()

        // Paused elapsed time remains 120s
        assertEquals(120L, timer.getElapsedStudySeconds())
    }

    // Verifies that finishing a study interval transitions state to RESTING
    @Test
    fun update_whenStudyIntervalFinishes_startsResting() {
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()

        // End of interval
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Assert
        assertEquals(TimerState.RESTING, stateMachine.state)
        assertEquals(0, timer.currentIntervalIndex)
        assertEquals(120_000L, timer.endTimeMillis)
        assertEquals(60L, timer.remainingSeconds)
    }

    // Verifies that finishing a rest period advances to the next interval
    @Test
    fun update_whenRestFinishes_startsNextInterval() {
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()

        // End of interval
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // End of rest
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Assert
        assertEquals(TimerState.STUDYING, stateMachine.state)
        assertEquals(1, timer.currentIntervalIndex)
        assertEquals(180_000L, timer.endTimeMillis)
        assertEquals(60L, timer.remainingSeconds)
    }

    // Verifies that finishing the last interval transitions state to FINISHED
    @Test
    fun update_whenLastIntervalFinishes_finishesSession() {
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()

        // Interval 1
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Rest 1
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Interval 2
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Rest 2
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Interval 3
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Assert
        assertEquals(TimerState.FINISHED, stateMachine.state)
        assertEquals(2, timer.currentIntervalIndex)
        assertNull(timer.endTimeMillis)
        assertEquals(0L, timer.remainingSeconds)
    }

    // Verifies pausing saves remaining time and stops active timer
    @Test
    fun pause_savesRemainingTime() {
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()

        // Advance 20s
        fakeClock.advanceMillis(20_000L)

        // Pause
        val paused = timer.pause()

        // Assert
        assertTrue(paused)
        assertEquals(TimerState.PAUSED, stateMachine.state)
        assertNull(timer.endTimeMillis)
        assertEquals(40L, timer.remainingSeconds)
    }

    // Verifies resuming from PAUSED recalculates endTimeMillis correctly
    @Test
    fun resume_restoresEndTime() {
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()

        // Advance 20s and pause
        fakeClock.advanceMillis(20_000L)
        timer.pause()

        // Advance fake clock while paused
        fakeClock.advanceMillis(10_000L)

        // Resume
        val resumed = timer.resume()

        // Assert
        assertTrue(resumed)
        assertEquals(TimerState.STUDYING, stateMachine.state)

        // remainingMillis = 40s (40000)
        // clock.nowMillis() = 30000
        // endTimeMillis = 70000
        assertEquals(70_000L, timer.endTimeMillis)
        assertEquals(40L, timer.remainingSeconds)
    }

    // Verifies resetting clears state and counters
    @Test
    fun reset_resetsTimer() {
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()
        fakeClock.advanceMillis(20_000L)
        timer.reset()

        // Assert
        assertEquals(TimerState.IDLE, stateMachine.state)
        assertEquals(0, timer.currentIntervalIndex)
        assertNull(timer.endTimeMillis)
        assertEquals(0L, timer.remainingSeconds)
    }
}
