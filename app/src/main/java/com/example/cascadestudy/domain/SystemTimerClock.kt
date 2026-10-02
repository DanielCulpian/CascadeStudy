package com.example.cascadestudy.domain

class SystemTimerClock: TimerClock {
    override fun nowMillis(): Long {
        return System.currentTimeMillis()
    }
}