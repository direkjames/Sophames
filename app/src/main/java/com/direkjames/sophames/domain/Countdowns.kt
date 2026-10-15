package com.direkjames.sophames.domain

import com.direkjames.sophames.data.EventEntity
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.min

enum class CountdownKind { ANNIVERSARY, MONTHSARY, MILESTONE, CUSTOM }

data class Countdown(
    val id: String,
    val kind: CountdownKind,
    val title: String,
    val date: LocalDate,
    /** Negative when a one-off custom date has already passed. */
    val daysLeft: Long,
    /** 0..1 — how far we are between the previous occurrence and this one. */
    val progress: Float,
    val customId: String? = null,
)

data class Together(val days: Long, val years: Int, val months: Int, val extraDays: Int)

fun togetherFor(start: LocalDate, today: LocalDate): Together {
    if (today.isBefore(start)) return Together(0, 0, 0, 0)
    val p = Period.between(start, today)
    return Together(ChronoUnit.DAYS.between(start, today), p.years, p.months, p.days)
}

fun ordinal(n: Int): String {
    val suffix = if (n % 100 in 11..13) "th" else when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$n$suffix"
}

fun daysLabel(daysLeft: Long): String = when {
    daysLeft < 0L -> "Passed"
    daysLeft == 0L -> "Today"
    daysLeft == 1L -> "Tomorrow"
    else -> "in $daysLeft days"
}

/** 100, 200 … 1000, then 1500, 2000 … 5000, then 7500 and 10000. */
private val MILESTONES: List<Int> =
    (100..1000 step 100).toList() + (1500..5000 step 500).toList() + listOf(7500, 10000)

private fun progressBetween(prev: LocalDate, next: LocalDate, today: LocalDate): Float {
    val total = ChronoUnit.DAYS.between(prev, next).toFloat()
    if (total <= 0f) return 1f
    return (ChronoUnit.DAYS.between(prev, today) / total).coerceIn(0f, 1f)
}

fun nextAnniversary(start: LocalDate, today: LocalDate): Countdown {
    var n = 1
    while (start.plusYears(n.toLong()).isBefore(today)) n++
    val date = start.plusYears(n.toLong())
    val prev = start.plusYears((n - 1).toLong())
    return Countdown(
        id = "anniversary-$n",
        kind = CountdownKind.ANNIVERSARY,
        title = "${ordinal(n)} anniversary",
        date = date,
        daysLeft = ChronoUnit.DAYS.between(today, date),
        progress = progressBetween(prev, date, today),
    )
}

/** Monthly dates (the 18th, for us). Every 12th month is the anniversary, so it is skipped here. */
fun nextMonthsary(start: LocalDate, today: LocalDate): Countdown {
    var k = 1
    while (true) {
        val date = start.plusMonths(k.toLong())
        if (!date.isBefore(today) && k % 12 != 0) {
            val prev = start.plusMonths((k - 1).toLong())
            return Countdown(
                id = "monthsary-$k",
                kind = CountdownKind.MONTHSARY,
                title = "${ordinal(k)} monthsary",
                date = date,
                daysLeft = ChronoUnit.DAYS.between(today, date),
                progress = progressBetween(prev, date, today),
            )
        }
        k++
    }
}

fun nextDaysMilestone(start: LocalDate, today: LocalDate): Countdown? {
    val elapsed = ChronoUnit.DAYS.between(start, today)
    val idx = MILESTONES.indexOfFirst { it >= elapsed }
    if (idx == -1) return null
    val n = MILESTONES[idx]
    val prevN = if (idx == 0) 0 else MILESTONES[idx - 1]
    val date = start.plusDays(n.toLong())
    return Countdown(
        id = "days-$n",
        kind = CountdownKind.MILESTONE,
        title = "$n days together",
        date = date,
        daysLeft = ChronoUnit.DAYS.between(today, date),
        progress = progressBetween(start.plusDays(prevN.toLong()), date, today),
    )
}

private fun inYear(base: LocalDate, year: Int): LocalDate {
    val day = min(base.dayOfMonth, YearMonth.of(year, base.month).lengthOfMonth())
    return LocalDate.of(year, base.month, day)
}

fun customCountdown(event: EventEntity, today: LocalDate): Countdown {
    val base = LocalDate.ofEpochDay(event.epochDay)
    val date: LocalDate
    val progress: Float
    if (event.repeatYearly) {
        var next = inYear(base, today.year)
        if (next.isBefore(today)) next = inYear(base, today.year + 1)
        date = next
        progress = progressBetween(inYear(base, next.year - 1), next, today)
    } else {
        date = base
        val left = ChronoUnit.DAYS.between(today, base)
        progress = if (left < 0) 1f else 1f - (min(left, 365L) / 365f)
    }
    return Countdown(
        id = "custom-${event.id}",
        kind = CountdownKind.CUSTOM,
        title = event.title,
        date = date,
        daysLeft = ChronoUnit.DAYS.between(today, date),
        progress = progress,
        customId = event.id,
    )
}

/** Everything coming up, soonest first. One-off custom dates that already passed are left out. */
fun buildUpcoming(start: LocalDate, today: LocalDate, events: List<EventEntity>): List<Countdown> {
    val list = mutableListOf(nextAnniversary(start, today), nextMonthsary(start, today))
    nextDaysMilestone(start, today)?.let { list.add(it) }
    events.map { customCountdown(it, today) }.filter { it.daysLeft >= 0 }.forEach { list.add(it) }
    return list.sortedBy { it.daysLeft }
}

/** Custom dates only, upcoming first and passed ones last. */
fun buildCustomList(events: List<EventEntity>, today: LocalDate): List<Countdown> =
    events.map { customCountdown(it, today) }
        .sortedWith(compareBy<Countdown> { it.daysLeft < 0 }.thenBy { if (it.daysLeft < 0) -it.daysLeft else it.daysLeft })
