package com.example.cascadestudy

import com.example.cascadestudy.domain.StudySessionPreset
import com.example.cascadestudy.domain.toStudySession
import org.junit.Assert.assertEquals
import org.junit.Test

class StudySessionPresetTest {

    // Tests the conversion of StudySessionPreset to StudySession with FULL preset
    @Test
    fun fullPreset_createsFullSession() {
        // Set
        val session = StudySessionPreset.FULL.toStudySession()

        // Assert
        assertEquals(
            listOf(60, 50, 40, 30, 20, 10),
            session.intervals
        )

        assertEquals(
            10,
            session.restDurationMinutes
        )
    }

    // Tests the conversion of StudySessionPreset to StudySession with SHORT preset
    @Test
    fun shortPreset_createsShortSession() {
        // Set
        val session = StudySessionPreset.SHORT.toStudySession()

        // Assert
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