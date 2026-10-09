package com.danielculpian.cascadestudy.domain

// Interface abstraction for obtaining current time in milliseconds
interface TimerClock {
    fun nowMillis(): Long
}
