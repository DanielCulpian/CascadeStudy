package com.example.cascadestudy

import com.example.cascadestudy.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// Unit tests for SessionRepository business logic and statistics formatting using FakeCompletedSessionDao
class SessionRepositoryTest {

    private lateinit var fakeDao: FakeCompletedSessionDao
    private lateinit var repository: SessionRepository

    @Before
    fun setup() {
        fakeDao = FakeCompletedSessionDao()
        repository = SessionRepository(
            dao = fakeDao,
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    // Verifies saving a completed session updates the totalStudySeconds flow
    @Test
    fun saveCompletedSession_updatesTotalStudySeconds() = runBlocking {
        // Set
        repository.saveCompletedSession(
            durationSeconds = 3600,
            presetType = "FULL"
        )

        // Act
        val totalSeconds = repository.totalStudySeconds.first()

        // Assert
        assertEquals(3600L, totalSeconds)
    }

    // Verifies saving multiple completed sessions calculates the cumulative total
    @Test
    fun multipleSessions_accumulateInTotalStudySeconds() = runBlocking {
        // Set
        repository.saveCompletedSession(
            durationSeconds = 3600,
            presetType = "FULL"
        )
        repository.saveCompletedSession(
            durationSeconds = 1800,
            presetType = "SHORT"
        )

        // Act
        val totalSeconds = repository.totalStudySeconds.first()

        // Assert
        assertEquals(5400L, totalSeconds)
    }

    // Verifies getWeeklyStudySeconds returns cumulative study seconds for the week
    @Test
    fun getWeeklyStudySeconds_returnsWeeklyTotal() = runBlocking {
        // Set
        repository.saveCompletedSession(
            durationSeconds = 2400,
            presetType = "SHORT"
        )

        // Act
        val weeklySeconds = repository.getWeeklyStudySeconds().first()

        // Assert
        assertEquals(2400L, weeklySeconds)
    }
}
