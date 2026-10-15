package com.direkjames.sophames.ui

import com.direkjames.sophames.domain.Together
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FULL = DateTimeFormatter.ofPattern("MMMM d, yyyy")
private val SHORT = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
private val MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy")

/** "January 18, 2025" */
fun LocalDate.fullLabel(): String = format(FULL)

/** "Sun, Jan 18, 2027" */
fun LocalDate.shortLabel(): String = format(SHORT)

/** "October 2026" */
fun LocalDate.monthYearLabel(): String = format(MONTH_YEAR)

private fun plural(n: Int, unit: String) = if (n == 1) "$n $unit" else "$n ${unit}s"

/** "1 year · 9 months · 0 days" — leading zero units are dropped. */
fun Together.breakdownLabel(): String {
    val parts = mutableListOf<String>()
    if (years > 0) parts += plural(years, "year")
    if (years > 0 || months > 0) parts += plural(months, "month")
    parts += plural(extraDays, "day")
    return parts.joinToString("  ·  ")
}
