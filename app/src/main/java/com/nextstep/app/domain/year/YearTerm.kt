package com.nextstep.app.domain.year

import java.time.LocalDate
import java.time.YearMonth

/** 올해 할 일의 때: 1학기(3~8월), 2학기(9~2월), 1년 내내. 학교 한 해는 3월에 시작합니다. */
enum class YearTerm(val label: String, val months: String) {
    FIRST("1학기", "3–8월"),
    SECOND("2학기", "9–2월"),
    ALL_YEAR("1년 내내", "3–2월");

    /** [today] 가 속한 학교 한 해 안에서 이 때의 마지막 날. */
    fun endDate(today: LocalDate): LocalDate {
        val startYear = schoolYearStart(today)
        return when (this) {
            FIRST -> LocalDate.of(startYear, FIRST_TERM_LAST_MONTH, LAST_DAY_OF_AUGUST)
            SECOND, ALL_YEAR -> YearMonth.of(startYear + 1, LAST_MONTH).atEndOfMonth()
        }
    }

    companion object {
        private const val SCHOOL_START_MONTH = 3
        private const val FIRST_TERM_LAST_MONTH = 8
        private const val LAST_DAY_OF_AUGUST = 31
        private const val LAST_MONTH = 2

        fun schoolYearStart(today: LocalDate): Int = if (today.monthValue >= SCHOOL_START_MONTH) today.year else today.year - 1

        /** 지금의 학기. */
        fun current(today: LocalDate): YearTerm = if (today.monthValue in SCHOOL_START_MONTH..FIRST_TERM_LAST_MONTH) FIRST else SECOND
    }
}
