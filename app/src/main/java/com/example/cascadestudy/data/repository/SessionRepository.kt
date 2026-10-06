package com.example.cascadestudy.data.repository

import com.example.cascadestudy.data.local.CompletedSessionDao
import com.example.cascadestudy.data.local.CompletedSessionEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

// Repository abstracting data operations and calculations for completed study sessions
class SessionRepository(
    private val dao: CompletedSessionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // Reactive Flow exposing total historical study time in seconds
    val totalStudySeconds: Flow<Long> = dao.getTotalStudySeconds().map { it ?: 0L }

    // Calculates start of the current week and returns a Flow of weekly study seconds
    fun getWeeklyStudySeconds(): Flow<Long> {
        val calendar: Calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dao.getWeeklyStudySeconds(calendar.timeInMillis).map { it ?: 0L }
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
