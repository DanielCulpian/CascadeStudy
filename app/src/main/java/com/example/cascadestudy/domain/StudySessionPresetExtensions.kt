package com.example.cascadestudy.domain

fun StudySessionPreset.toStudySession(): StudySession {
    return when (this) {
        StudySessionPreset.FULL -> StudySession(
            intervals = listOf(60, 50, 40, 30, 20, 10),
            restDurationMinutes = 10
        )

        StudySessionPreset.SHORT -> StudySession(
            intervals = listOf(30, 20, 10),
            restDurationMinutes = 10
        )

        else -> throw IllegalArgumentException("Unsupported preset")
    }
}