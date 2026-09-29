package com.nextstep.app.domain.period

import java.time.LocalDate
import java.time.YearMonth

/** 월간·학기 기간 계산. 학기는 학년도 기준(1학기 3월 1일~8월 31일, 2학기 9월 1일~다음 해 2월 말일). */
object Periods {

    fun current(kind: PeriodKind, today: LocalDate): Period = when (kind) {
        PeriodKind.MONTH -> month(YearMonth.from(today))
        PeriodKind.TERM -> term(today)
    }

    fun previous(period: Period): Period = current(period.kind, period.start.minusDays(1))

    private fun month(ym: YearMonth) = Period(PeriodKind.MONTH, ym.atDay(1), ym.atEndOfMonth(), "${ym.year}년 ${ym.monthValue}월")

    private fun term(day: LocalDate): Period {
        val first = day.monthValue in FIRST_TERM
        val schoolYear = if (day.monthValue >= FIRST_TERM.first) day.year else day.year - 1
        return if (first) {
            Period(PeriodKind.TERM, LocalDate.of(schoolYear, FIRST_TERM.first, 1), YearMonth.of(schoolYear, FIRST_TERM.last).atEndOfMonth(), "${schoolYear}학년도 1학기")
        } else {
            Period(PeriodKind.TERM, LocalDate.of(schoolYear, SECOND_TERM_START, 1), YearMonth.of(schoolYear + 1, 2).atEndOfMonth(), "${schoolYear}학년도 2학기")
        }
    }

    private val FIRST_TERM = 3..8
    private const val SECOND_TERM_START = 9
}
