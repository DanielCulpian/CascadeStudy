package com.danielculpian.cascadestudy

import com.danielculpian.cascadestudy.domain.StudySession
import org.junit.Test

// Unit tests for StudySession input parameter validations
class StudySessionTest {

    // Valid study session with positive intervals and rest duration
    @Test(expected = IllegalArgumentException::class)
    fun emptyIntervals_areNotAllowed() {
        StudySession(
            intervals = emptyList(),
            restDurationMinutes = 10
        )
    }

    // Valid study session with positive intervals and rest duration
    @Test(expected = IllegalArgumentException::class)
    fun nonPositiveIntervals_areNotAllowed() {
        StudySession(
            intervals = listOf(60, 0, 30),
            restDurationMinutes = 10
        )
    }

    // Valid study session with positive intervals and rest duration
    @Test(expected = IllegalArgumentException::class)
    fun nonPositiveRestDuration_isNotAllowed() {
        StudySession(
            intervals = listOf(60, 50, 40),
            restDurationMinutes = 0
        )
    }
}
