package com.nextstep.app.domain.period

import com.nextstep.app.domain.time.SchoolYear
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
        val year = SchoolYear.of(day)
        return if (SchoolYear.isFirstTerm(day)) {
            Period(PeriodKind.TERM, SchoolYear.start(year), SchoolYear.firstTermEnd(year), "${year}학년도 1학기")
        } else {
            Period(PeriodKind.TERM, SchoolYear.secondTermStart(year), SchoolYear.end(year), "${year}학년도 2학기")
        }
    }
}
