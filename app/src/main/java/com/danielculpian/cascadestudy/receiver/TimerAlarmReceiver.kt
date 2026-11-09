package com.danielculpian.cascadestudy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.danielculpian.cascadestudy.domain.StudySessionPreset
import com.danielculpian.cascadestudy.domain.toStudySession
import com.danielculpian.cascadestudy.notification.NotificationHelper
import com.danielculpian.cascadestudy.sound.SoundManager

// BroadcastReceiver waking the CPU at exact interval completion timestamps to trigger sound and notification
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
        } else {
            // Regular study interval finished -> Rest started
            notificationHelper.showRestStartedNotification()
            soundManager.playFinishStudySound()
        }
    }

    private fun getPresetTotalIntervals(presetName: String): Int {
        val preset = try {
            StudySessionPreset.valueOf(presetName)
        } catch (_: Exception) {
            StudySessionPreset.FULL
        }
        return preset.toStudySession().intervals.size
    }
}
