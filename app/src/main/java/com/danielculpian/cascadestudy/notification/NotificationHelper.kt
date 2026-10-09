package com.danielculpian.cascadestudy.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.danielculpian.cascadestudy.R

// Helper class for creating notification channels and posting study timer notifications
open class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "cascade_study_channel"
        private const val CHANNEL_NAME = "Notificaciones de Estudio"
        private const val CHANNEL_DESCRIPTION = "Avisos de inicio, descansos y fin de sesión"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    // Creates the notification channel required for Android 8.0+
    protected open fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Posts a notification when a new study interval starts
    open fun showIntervalStartedNotification(currentInterval: Int, totalIntervals: Int) {
        showNotification(
            title = "¡Intervalo iniciado!",
            message = "Iniciado intervalo $currentInterval de $totalIntervals. ¡A concentrarse!"
        )
    }

    // Posts a notification when a study interval ends and rest period starts
    open fun showRestStartedNotification() {
        showNotification(
            title = "¡Tiempo de descanso!",
            message = "Has completado el intervalo de estudio. Tómate un descanso."
        )
    }

    // Posts a notification when the entire study session is finished
    open fun showSessionFinishedNotification() {
        showNotification(
            title = "¡Sesión finalizada!",
            message = "¡Felicidades! Has completado con éxito toda la sesión de estudio."
        )
    }

    // Builds and displays a notification if permission is granted
    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Notification permission denied
        }
    }
}
