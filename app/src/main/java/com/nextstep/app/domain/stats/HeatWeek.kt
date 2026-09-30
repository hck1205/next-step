package com.nextstep.app.domain.stats

import java.time.LocalDate

/** 공부 달력의 한 주(월~일). */
data class HeatWeek(val monday: LocalDate, val days: List<HeatDay>) {
    val activeDays: Int get() = days.count { it.minutes > 0 }
    val totalMinutes: Int get() = days.sumOf { it.minutes }
}
