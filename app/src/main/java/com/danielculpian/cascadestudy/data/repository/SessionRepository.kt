package com.danielculpian.cascadestudy.data.repository

import com.danielculpian.cascadestudy.data.local.CompletedSessionDao
import com.danielculpian.cascadestudy.data.local.CompletedSessionEntity
import com.danielculpian.cascadestudy.domain.WeeklySummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
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

    // Calculates start of today (00:00:00) and returns a Flow of daily study seconds
    fun getDailyStudySeconds(): Flow<Long> {
        val startOfDayMillis = LocalDate.now(ZoneId.systemDefault())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return dao.getDailyStudySeconds(startOfDayMillis).map { it ?: 0L }
    }

    // Exposes a Flow of all closed weekly summaries (Monday to Sunday) prior to the current week
    fun getClosedWeeklySummaries(): Flow<List<WeeklySummary>> {
        val zoneId = ZoneId.systemDefault()

        return dao.getAllSessions().map { sessions ->
            if (sessions.isEmpty()) {
                emptyList()
            } else {
                val currentMonday = LocalDate.now(zoneId).with(DayOfWeek.MONDAY)
                val earliestTimestamp = sessions.minOf { it.timestampEpochMillis }
                val earliestDate = Instant.ofEpochMilli(earliestTimestamp).atZone(zoneId).toLocalDate()
                val startMonday = earliestDate.with(DayOfWeek.MONDAY)

                val summaries = mutableListOf<WeeklySummary>()
                var cursorMonday = startMonday

                while (cursorMonday.isBefore(currentMonday)) {
                    val cursorSunday = cursorMonday.plusDays(6)
                    val startMillis = cursorMonday.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val endMillis = cursorSunday.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()

                    val weekSessionsSum = sessions
                        .filter { it.timestampEpochMillis in startMillis..endMillis }
                        .sumOf { it.durationSeconds }

                    summaries.add(WeeklySummary.create(cursorMonday, weekSessionsSum))
                    cursorMonday = cursorMonday.plusWeeks(1)
                }

                // Return sorted with the most recent closed week first
                summaries.sortedByDescending { it.startOfWeekMillis }
            }
        }
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
