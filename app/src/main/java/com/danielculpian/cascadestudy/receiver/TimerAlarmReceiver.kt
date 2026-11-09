package com.danielculpian.cascadestudy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.danielculpian.cascadestudy.data.local.AppDatabase
import com.danielculpian.cascadestudy.data.repository.SessionRepository
import com.danielculpian.cascadestudy.domain.StudySessionPreset
import com.danielculpian.cascadestudy.domain.toStudySession
import com.danielculpian.cascadestudy.notification.NotificationHelper
import com.danielculpian.cascadestudy.sound.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// BroadcastReceiver waking the CPU at exact interval completion timestamps to trigger sound, notification, and DB persistence
class TimerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val intervalIndex = intent.getIntExtra("EXTRA_INTERVAL_INDEX", 0)
        val presetName = intent.getStringExtra("EXTRA_PRESET_NAME") ?: "FULL"
        val isLastInterval = intent.getBooleanExtra("EXTRA_IS_LAST_INTERVAL", false)
        val isRest = intent.getBooleanExtra("EXTRA_IS_REST", false)

        val notificationHelper = NotificationHelper(context)
        val soundManager = SoundManager(context)

        if (isRest) {
            // Rest break finished -> Start next interval notification & sound
            notificationHelper.showIntervalStartedNotification(intervalIndex + 2, getPresetTotalIntervals(presetName))
            soundManager.playStartStudySound()
        } else if (isLastInterval) {
            // Last study interval finished -> Session completed
            notificationHelper.showSessionFinishedNotification()
            soundManager.playFinishSessionSound()

            saveIntervalToDatabase(context, presetName, intervalIndex)
        } else {
            // Regular study interval finished -> Rest started
            notificationHelper.showRestStartedNotification()
            soundManager.playFinishStudySound()

            saveIntervalToDatabase(context, presetName, intervalIndex)
        }
    }

    private fun getPresetTotalIntervals(presetName: String): Int {
        val preset = try {
            StudySessionPreset.valueOf(presetName)
        } catch (e: Exception) {
            StudySessionPreset.FULL
        }
        return preset.toStudySession().intervals.size
    }

    private fun saveIntervalToDatabase(context: Context, presetName: String, intervalIndex: Int) {
        val preset = try {
            StudySessionPreset.valueOf(presetName)
        } catch (e: Exception) {
            StudySessionPreset.FULL
        }
        val session = preset.toStudySession()

        if (intervalIndex in session.intervals.indices) {
            val durationSeconds = session.intervals[intervalIndex] * 60L
            val database = AppDatabase.getDatabase(context.applicationContext)
            val repository = SessionRepository(database.completedSessionDao())

            CoroutineScope(Dispatchers.IO).launch {
                repository.saveCompletedSession(durationSeconds, presetName)
            }
        }
    }
}
