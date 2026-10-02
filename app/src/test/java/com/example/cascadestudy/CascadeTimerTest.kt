package com.example.cascadestudy

import com.example.cascadestudy.domain.CascadeTimer
import com.example.cascadestudy.domain.StudySession
import com.example.cascadestudy.domain.TimerState
import com.example.cascadestudy.domain.TimerStateMachine
import org.junit.Test

import org.junit.Assert.*

class CascadeTimerTest {

    @Test
    fun start_startsStudying(){
        // Set
        val session = StudySession(
            intervals = listOf(1, 1, 1),
            restDurationMinutes = 1
        )

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

    @Test
    fun update_whenStudyIntervalFinishes_startsResting(){
        // Set
        val session = StudySession(
            intervals = listOf(1, 1, 1),
            restDurationMinutes = 1
        )

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

    @Test
    fun update_whenRestIntervalFinishes_startsNextInterval(){
        // Set
        val session = StudySession(
            intervals = listOf(1, 1, 1),
            restDurationMinutes = 1
        )

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

    @Test
    fun update_whenLastIntervalFinishes_finishesSession(){
        // Set
        val session = StudySession(
            intervals = listOf(1, 1, 1),
            restDurationMinutes = 1
        )

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

    @Test
    fun pause_whenStudying_pausesTimer(){
        // Set
        val session = StudySession(
            intervals = listOf(1, 1, 1),
            restDurationMinutes = 1
        )

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

    @Test
    fun resume_whenPaused_resumesTimer(){
        // Set
        val session = StudySession(
            intervals = listOf(1, 1, 1),
            restDurationMinutes = 1
        )

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

    @Test
    fun reset_whenStudying_resetsTimer(){
        // Set
        val session = StudySession(
            intervals = listOf(1, 1, 1),
            restDurationMinutes = 1
        )

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

    @Test
    fun pause_whenIdle_doesNothing(){
        // Set
        val session = StudySession(
            intervals = listOf(1, 1, 1),
            restDurationMinutes = 1
        )

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

}

