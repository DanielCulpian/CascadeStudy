package com.danielculpian.cascadestudy.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object AlarmScheduler {
    private const val ALARM_REQUEST_CODE = 3003

    fun scheduleAlarm(
        context: Context,
        triggerAtMillis: Long,
        intervalIndex: Int,
        presetName: String,
        isLastInterval: Boolean,
        isRest: Boolean
    ) {
        if (triggerAtMillis <= 0L) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            putExtra("EXTRA_INTERVAL_INDEX", intervalIndex)
            putExtra("EXTRA_PRESET_NAME", presetName)
            putExtra("EXTRA_IS_LAST_INTERVAL", isLastInterval)
            putExtra("EXTRA_IS_REST", isRest)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // setAlarmClock guarantees 100% exact wakeup and bypasses Doze mode on all devices
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: Exception) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    fun cancelAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, TimerAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
