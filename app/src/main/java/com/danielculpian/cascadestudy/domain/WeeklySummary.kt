package com.danielculpian.cascadestudy.domain

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

// Data class representing a closed weekly study summary (Monday to Sunday)
data class WeeklySummary(
    val year: Int,
    val month: Int,            // 1..12
    val monthName: String,     // e.g. "Diciembre"
    val weekLabel: String,     // e.g. "2-8 de Diciembre/2026"
    val startOfWeekMillis: Long,
    val endOfWeekMillis: Long,
    val totalSeconds: Long
) {
    val totalHours: Float
        get() = totalSeconds / 3600f

    companion object {
        private val SPANISH_LOCALE = Locale.forLanguageTag("es-ES")

        // Helper function to create a WeeklySummary instance from Monday LocalDate and total study seconds
        fun create(mondayDate: LocalDate, totalSeconds: Long): WeeklySummary {
            val sundayDate = mondayDate.plusDays(6)
            val zoneId = ZoneId.systemDefault()

            val startMillis = mondayDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = sundayDate.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()

            val startDay = mondayDate.dayOfMonth
            val endDay = sundayDate.dayOfMonth

            val startMonthName = mondayDate.month.getDisplayName(TextStyle.FULL, SPANISH_LOCALE)
                .replaceFirstChar { it.uppercase(SPANISH_LOCALE) }
            val endMonthName = sundayDate.month.getDisplayName(TextStyle.FULL, SPANISH_LOCALE)
                .replaceFirstChar { it.uppercase(SPANISH_LOCALE) }

            val weekLabel = if (mondayDate.month == sundayDate.month) {
                "$startDay-$endDay de $startMonthName/${mondayDate.year}"
            } else {
                "$startDay de $startMonthName - $endDay de $endMonthName/${sundayDate.year}"
            }

            return WeeklySummary(
                year = sundayDate.year,
                month = sundayDate.monthValue,
                monthName = endMonthName,
                weekLabel = weekLabel,
                startOfWeekMillis = startMillis,
                endOfWeekMillis = endMillis,
                totalSeconds = totalSeconds
            )
        }
    }
}
