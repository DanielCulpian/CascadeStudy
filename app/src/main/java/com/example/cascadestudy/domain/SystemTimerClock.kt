package com.example.cascadestudy.domain

// Default implementation of TimerClock using System.currentTimeMillis()
class SystemTimerClock: TimerClock {
    override fun nowMillis(): Long {
        return System.currentTimeMillis()
    }
}