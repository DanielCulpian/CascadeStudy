package com.example.cascadestudy.domain

// Interface abstraction for obtaining current time in milliseconds (useful for unit testing)
interface TimerClock {
    fun nowMillis(): Long
}