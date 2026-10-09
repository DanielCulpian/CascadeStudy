package com.danielculpian.cascadestudy

import com.danielculpian.cascadestudy.domain.TimerClock

// Fake implementation of TimerClock for testing
class FakeTimerClock(
    private var currentMillis: Long = 0L
) : TimerClock {

    override fun nowMillis(): Long {
        return currentMillis
    }

    fun advanceMillis(millis: Long) {
        currentMillis += millis
    }
}
