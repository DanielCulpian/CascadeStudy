package com.example.cascadestudy

import com.example.cascadestudy.data.local.CompletedSessionDao
import com.example.cascadestudy.data.local.CompletedSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCompletedSessionDao : CompletedSessionDao {
    private val sessions = mutableListOf<CompletedSessionEntity>()
    private val totalSecondsFlow = MutableStateFlow<Long?>(0L)
    private val weeklySecondsFlow = MutableStateFlow<Long?>(0L)

    override fun insertSession(session: CompletedSessionEntity) {
        sessions.add(session)
        val total = sessions.sumOf { it.durationSeconds }
        totalSecondsFlow.value = total
        weeklySecondsFlow.value = total
    }

    override fun getTotalStudySeconds(): Flow<Long?> = totalSecondsFlow

    override fun getWeeklyStudySeconds(startOfWeekMillis: Long): Flow<Long?> = weeklySecondsFlow
}
