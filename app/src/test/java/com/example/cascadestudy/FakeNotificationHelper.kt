package com.example.cascadestudy

import android.content.ContextWrapper
import com.example.cascadestudy.notification.NotificationHelper

internal class TestDummyContext : ContextWrapper(null)

// Fake NotificationHelper implementation for verifying notification triggers in unit tests
class FakeNotificationHelper : NotificationHelper(TestDummyContext()) {
    var intervalStartedCount = 0
        private set
    var restStartedCount = 0
        private set
    var sessionFinishedCount = 0
        private set

    override fun createNotificationChannel() {
        // No-op for JVM unit tests
    }

    override fun showIntervalStartedNotification(currentInterval: Int, totalIntervals: Int) {
        intervalStartedCount++
    }

    override fun showRestStartedNotification() {
        restStartedCount++
    }

    override fun showSessionFinishedNotification() {
        sessionFinishedCount++
    }
}
