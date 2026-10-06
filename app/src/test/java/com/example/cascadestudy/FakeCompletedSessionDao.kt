package com.example.cascadestudy

import com.example.cascadestudy.data.local.CompletedSessionDao
import com.example.cascadestudy.data.local.CompletedSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Fake implementation of CompletedSessionDao for testing
class FakeCompletedSessionDao : CompletedSessionDao {
    private val sessions = mutableListOf<CompletedSessionEntity>()
    private val totalSecondsFlow = MutableStateFlow<Long?>(0L)
    private val weeklySecondsFlow = MutableStateFlow<Long?>(0L)

    // Mock implementation of insertSession
    override fun insertSession(session: CompletedSessionEntity) {
        sessions.add(session)
        val total = sessions.sumOf { it.durationSeconds }
        totalSecondsFlow.value = total
        weeklySecondsFlow.value = total
    }

    // Mock implementation of getTotalStudySeconds
    override fun getTotalStudySeconds(): Flow<Long?> = totalSecondsFlow

    // Mock implementation of getWeeklyStudySeconds
    override fun getWeeklyStudySeconds(startOfWeekMillis: Long): Flow<Long?> = weeklySecondsFlow
}
