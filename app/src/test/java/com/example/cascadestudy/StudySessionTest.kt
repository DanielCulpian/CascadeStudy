package com.example.cascadestudy

import com.example.cascadestudy.domain.StudySession
import org.junit.Test

class StudySessionTest {

    @Test(expected = IllegalArgumentException::class)
    fun emptyIntervals_areNotAllowed(){
        StudySession(
            intervals = emptyList(),
            restDurationMinutes = 10
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonPositiveIntervals_areNotAllowed(){
        StudySession(
            intervals = listOf(60, 0, 30),
            restDurationMinutes = 10
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonPositiveRestDuration_isNotAllowed(){
        StudySession(
            intervals = listOf(60, 50, 40),
            restDurationMinutes = 0
        )
    }
}