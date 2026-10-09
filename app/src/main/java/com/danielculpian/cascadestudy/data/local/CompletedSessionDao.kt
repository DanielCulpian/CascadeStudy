package com.danielculpian.cascadestudy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Data Access Object defining database queries for completed study sessions
@Dao
interface CompletedSessionDao {
    // Inserts a new completed session record into the database
    @Insert
    fun insertSession(session: CompletedSessionEntity)

    // Returns a Flow emitting the total cumulative study time in seconds
    @Query("SELECT SUM(durationSeconds) FROM completed_sessions")
    fun getTotalStudySeconds(): Flow<Long?>

    // Returns a Flow emitting total study seconds recorded since the given start of week timestamp
    @Query("SELECT SUM(durationSeconds) FROM completed_sessions WHERE timestampEpochMillis >= :startOfWeekMillis")
    fun getWeeklyStudySeconds(startOfWeekMillis: Long): Flow<Long?>
}
