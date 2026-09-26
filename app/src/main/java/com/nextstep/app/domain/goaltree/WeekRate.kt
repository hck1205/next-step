package com.nextstep.app.domain.goaltree

import java.time.LocalDate

/** 한 주(월요일 시작)에 마감이던 할 일 중 끝낸 수. */
data class WeekRate(val weekStart: LocalDate, val due: Int, val done: Int) {
    val rate: Float? get() = if (due == 0) null else done.toFloat() / due
}
