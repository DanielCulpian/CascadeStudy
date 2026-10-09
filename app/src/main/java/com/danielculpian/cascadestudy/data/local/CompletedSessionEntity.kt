package com.danielculpian.cascadestudy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room database entity representing a single completed study session record
@Entity(tableName = "completed_sessions")
data class CompletedSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationSeconds: Long,
    val presetType: String,
    val timestampEpochMillis: Long = System.currentTimeMillis()
)
