package com.danielculpian.cascadestudy

import com.danielculpian.cascadestudy.domain.StudySessionPreset
import com.danielculpian.cascadestudy.domain.toStudySession
import org.junit.Assert.assertEquals
import org.junit.Test

// Unit tests for StudySessionPreset conversions
class StudySessionPresetTest {

    @Test
    fun fullPreset_createsFullSession() {
        val session = StudySessionPreset.FULL.toStudySession()

        assertEquals(
            listOf(60, 50, 40, 30, 20, 10),
            session.intervals
        )

        assertEquals(
            10,
            session.restDurationMinutes
        )
    }

    @Test
    fun shortPreset_createsShortSession() {
        val session = StudySessionPreset.SHORT.toStudySession()

        assertEquals(
            listOf(30, 20, 10),
            session.intervals
        )

        assertEquals(
            10,
            session.restDurationMinutes
        )
    }
}
