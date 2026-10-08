package com.example.cascadestudy.domain

// Data class representing interval durations and rest time for a study session
data class StudySession(
    val intervals: List<Int>,
    val restDurationMinutes: Int
) {
    // Validates the study session parameters
    init {
        require(intervals.isNotEmpty()) {
            "Study session must have at least one interval."
        }
        require(intervals.all { it > 0 }) {
            "Study session intervals must be positive."
        }
        require(restDurationMinutes > 0) {
            "Rest duration must be greater than zero."
        }
    }
}
