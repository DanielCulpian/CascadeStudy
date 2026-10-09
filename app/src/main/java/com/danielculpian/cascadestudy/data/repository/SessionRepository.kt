package com.danielculpian.cascadestudy.data.repository

import com.danielculpian.cascadestudy.data.local.CompletedSessionDao
import com.danielculpian.cascadestudy.data.local.CompletedSessionEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

// Repository abstracting data operations and calculations for completed study sessions
class SessionRepository(
    private val dao: CompletedSessionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // Reactive Flow exposing total historical study time in seconds
    val totalStudySeconds: Flow<Long> = dao.getTotalStudySeconds().map { it ?: 0L }

    // Calculates start of the current week (Monday 00:00:00) and returns a Flow of weekly study seconds
    fun getWeeklyStudySeconds(): Flow<Long> {
        val startOfWeekMillis = LocalDate.now(ZoneId.systemDefault())
            .with(DayOfWeek.MONDAY)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return dao.getWeeklyStudySeconds(startOfWeekMillis).map { it ?: 0L }
    }

    // Persists a newly completed study session record asynchronously on the IO dispatcher
    suspend fun saveCompletedSession(durationSeconds: Long, presetType: String) {
        withContext(ioDispatcher) {
            dao.insertSession(
                CompletedSessionEntity(
                    durationSeconds = durationSeconds,
                    presetType = presetType
                )
            )
        }
    }
}
