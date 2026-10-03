package com.example.cascadestudy

import com.example.cascadestudy.domain.CascadeTimer
import com.example.cascadestudy.domain.StudySession
import com.example.cascadestudy.domain.TimerState
import com.example.cascadestudy.domain.TimerStateMachine
import org.junit.Test

import org.junit.Assert.*

// Unit tests for CascadeTimer state transitions and time tracking
class CascadeTimerTest {

    // Virtual session to use in tests
    private val testSession = StudySession(
        intervals = listOf(1, 1, 1),
        restDurationMinutes = 1
    )

    // Verifies that starting a session transitions state to STUDYING and sets initial time
    @Test
    fun start_startsStudying(){
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

    // Verifies that finishing a study interval transitions state to RESTING
    @Test
    fun update_whenStudyIntervalFinishes_startsResting(){
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Assert
        assertEquals(TimerState.RESTING, stateMachine.state)
        assertEquals(0, timer.currentIntervalIndex)
        assertEquals(120_000L, timer.endTimeMillis)
        assertEquals(60L, timer.remainingSeconds)
    }

    // Verifies that finishing a rest period advances to the next study interval
    @Test
    fun update_whenRestIntervalFinishes_startsNextInterval(){
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()
        fakeClock.advanceMillis(60_000L)
        timer.update()
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Assert
        assertEquals(TimerState.STUDYING, stateMachine.state)
        assertEquals(1, timer.currentIntervalIndex)
        assertEquals(180_000L, timer.endTimeMillis)
        assertEquals(60L, timer.remainingSeconds)
    }

    // Verifies that finishing the final study interval transitions state to FINISHED
    @Test
    fun update_whenLastIntervalFinishes_finishesSession(){
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()
        fakeClock.advanceMillis(60_000L)
        timer.update()
        fakeClock.advanceMillis(60_000L)
        timer.update()
        fakeClock.advanceMillis(60_000L)
        timer.update()
        fakeClock.advanceMillis(60_000L)
        timer.update()
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Assert
        assertEquals(TimerState.FINISHED, stateMachine.state)
        assertEquals(2, timer.currentIntervalIndex)
        assertEquals(null, timer.endTimeMillis)
        assertEquals(0, timer.remainingSeconds)
    }

    // Verifies that pausing while studying saves remaining time and sets state to PAUSED
    @Test
    fun pause_whenStudying_pausesTimer(){
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()
        fakeClock.advanceMillis(20_000L)
        timer.pause()

        // Assert
        assertEquals(TimerState.PAUSED, stateMachine.state)
        assertEquals(0, timer.currentIntervalIndex)
        assertEquals(null, timer.endTimeMillis)
        assertEquals(40L, timer.remainingSeconds)
    }

    // Verifies that resuming from PAUSED state continues studying with calculated end time
    @Test
    fun resume_whenPaused_resumesTimer(){
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()
        fakeClock.advanceMillis(20_000L)
        timer.pause()
        timer.resume()
        fakeClock.advanceMillis(10_000L)
        timer.update()

        // Assert
        assertEquals(TimerState.STUDYING, stateMachine.state)
        assertEquals(0, timer.currentIntervalIndex)
        assertEquals(60_000L, timer.endTimeMillis)
        assertEquals(30L, timer.remainingSeconds)
    }

    // Verifies that resetting returns the timer to IDLE state and clears all values
    @Test
    fun reset_whenStudying_resetsTimer(){
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.start()
        fakeClock.advanceMillis(20_000L)
        timer.update()
        timer.reset()

        // Assert
        assertEquals(TimerState.IDLE, stateMachine.state)
        assertEquals(0, timer.currentIntervalIndex)
        assertEquals(null, timer.endTimeMillis)
        assertEquals(0L, timer.remainingSeconds)
    }

    // Verifies that calling pause while IDLE is ignored and keeps the timer IDLE
    @Test
    fun pause_whenIdle_doesNothing(){
        // Set
        val session = testSession

        val stateMachine = TimerStateMachine()
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(session, stateMachine, fakeClock)

        // Act
        timer.pause()

        // Assert
        assertEquals(TimerState.IDLE, stateMachine.state)
        assertEquals(0, timer.currentIntervalIndex)
        assertEquals(null, timer.endTimeMillis)
        assertEquals(0L, timer.remainingSeconds)
    }

    // Verifies if whe can save milliseconds
    @Test
    fun pauseAndResume_preservesRemainingMilliseconds(){
        // Set
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(
            session = testSession,
            stateMachine = TimerStateMachine(),
            clock = fakeClock
        )

        // Act
        timer.start()
        fakeClock.advanceMillis(20_123L)
        timer.update()
        timer.pause()

        // Assert
        assertEquals(TimerState.PAUSED, timer.state)
        assertEquals(39_877L, timer.remainingMillis)

        // Act
        timer.resume()
        fakeClock.advanceMillis(10_000L)
        timer.update()

        // Assert
        assertEquals(TimerState.STUDYING, timer.state)
        assertEquals(29_877L, timer.remainingMillis)
    }

    // Verifies that calling resume with no time remaining is ignored
    @Test
    fun resume_whenNoTimeRemains_doesNothing(){
        // Set
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(
            session = testSession,
            stateMachine = TimerStateMachine(),
            clock = fakeClock
        )

        // Act
        timer.start()
        fakeClock.advanceMillis(60_000L)
        timer.pause()
        val resumed = timer.resume()

        // Assert
        assertEquals(false, resumed)
        assertEquals(TimerState.PAUSED, timer.state)
        assertEquals(0L, timer.remainingMillis)
    }

    // Verifies that a session with only one interval finishes immediately
    @Test
    fun update_whenSessionHasOneInterval_finishesSession(){
        // Set
        val fakeClock = FakeTimerClock()
        val session = StudySession(
            intervals = listOf(1),
            restDurationMinutes = 1
        )
        val timer = CascadeTimer(
            session = session,
            stateMachine = TimerStateMachine(),
            clock = fakeClock
        )

        // Act
        timer.start()
        fakeClock.advanceMillis(60_000L)
        timer.update()

        // Assert
        assertEquals(TimerState.FINISHED, timer.state)
        assertEquals(0, timer.currentIntervalIndex)
        assertEquals(0L, timer.remainingMillis)
    }

    // Verifies that remaining seconds are calculated correctly
    @Test
    fun remainingSeconds_roundsUpRemainingMilliseconds(){
        // Set
        val fakeClock = FakeTimerClock()
        val timer = CascadeTimer(
            session = testSession,
            stateMachine = TimerStateMachine(),
            clock = fakeClock
        )

        // Act
        timer.start()
        fakeClock.advanceMillis(20_123L)
        timer.update()

        // Assert
        assertEquals(40L, timer.remainingSeconds)
    }
}

