package com.example.cascadestudy

import com.example.cascadestudy.domain.TimerClock

class FakeTimerClock(
    private var currentTimeMillis: Long = 0L
): TimerClock {
    override fun nowMillis(): Long {
        return currentTimeMillis
    }

    fun advanceMillis(millis: Long){
        currentTimeMillis += millis
    }

}