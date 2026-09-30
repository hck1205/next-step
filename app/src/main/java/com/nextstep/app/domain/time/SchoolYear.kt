package com.nextstep.app.domain.time

import java.time.LocalDate
import java.time.YearMonth

/**
 * 학교 한 해(학년도): 3월 1일 ~ 다음 해 2월 말. 1학기는 3~8월, 2학기는 9월 ~ 다음 해 2월.
 * 학년도·학기를 가르는 곳(학사일정 · 학년 계산 · 올해 할 일 · 여정 달력 · 기간 리포트 · 교육 프로젝트)이 모두 이것만 씁니다.
 */
object SchoolYear {
    const val START_MONTH = 3
    const val SECOND_TERM_START_MONTH = 9

    /** [date] 가 속한 학년도(시작하는 해). 1~2월은 앞 해의 학년도입니다. */
    fun of(date: LocalDate): Int = if (date.monthValue >= START_MONTH) date.year else date.year - 1

    fun start(year: Int): LocalDate = LocalDate.of(year, START_MONTH, 1)
    fun end(year: Int): LocalDate = YearMonth.of(year + 1, START_MONTH - 1).atEndOfMonth()

    fun firstTermEnd(year: Int): LocalDate = YearMonth.of(year, SECOND_TERM_START_MONTH - 1).atEndOfMonth()
    fun secondTermStart(year: Int): LocalDate = LocalDate.of(year, SECOND_TERM_START_MONTH, 1)

    /** [date] 가 1학기(3~8월)인지. */
    fun isFirstTerm(date: LocalDate): Boolean = date.monthValue in START_MONTH until SECOND_TERM_START_MONTH
}
