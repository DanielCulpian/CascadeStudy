package com.example.cascadestudy

import com.example.cascadestudy.domain.TimerClock

// Fake clock implementation for controlling time during unit tests
class FakeTimerClock(
    private var currentTimeMillis: Long = 0L
): TimerClock {
    override fun nowMillis(): Long {
        return currentTimeMillis
    }

    // Advances the internal clock by specified milliseconds
    fun advanceMillis(millis: Long){
        currentTimeMillis += millis
    }

}