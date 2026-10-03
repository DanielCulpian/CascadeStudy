package com.example.cascadestudy.domain

// To save the different intervals of study that the application can manage. All numbers must be read on minutes.
data class StudySession(
    // TODO: Set new intervals. Doing that we will be able to manage shortest sessions like (30, 20, 10)
    val intervals: List<Int> = listOf(60, 50, 40, 30, 20, 10), // Main study intervals
    val restDurationMinutes: Int = 10 // Main rest time
){
    // Validates the study session parameters
    init{
        require(intervals.isNotEmpty()){
            "Study session must have at least one interval."
        }
        require(intervals.all { it > 0 }){
            "Study session intervals must be positive."
        }
        require(restDurationMinutes > 0){
            "Rest duration must be greater than zero."
        }
    }
}
