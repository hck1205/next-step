package com.nextstep.app.domain.period

import java.time.LocalDate

/** 날짜 범위 한 벌([start]~[end], 양 끝 포함)과 이름(예: "2029년 3월", "2029학년도 1학기"). */
data class Period(val kind: PeriodKind, val start: LocalDate, val end: LocalDate, val label: String) {
    operator fun contains(date: LocalDate): Boolean = date in start..end
}
