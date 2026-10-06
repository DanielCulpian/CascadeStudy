package com.example.cascadestudy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room database manager for local persistence of study statistics
@Database(entities = [CompletedSessionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // Provides access to the completed sessions DAO
    abstract fun completedSessionDao(): CompletedSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Returns the singleton instance of AppDatabase, creating it if necessary
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
