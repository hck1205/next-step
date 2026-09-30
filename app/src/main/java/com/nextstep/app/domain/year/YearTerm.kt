package com.nextstep.app.domain.year

import com.nextstep.app.domain.time.SchoolYear
import java.time.LocalDate

/** 올해 할 일의 때: 1학기(3~8월), 2학기(9~2월), 1년 내내. 학교 한 해는 3월에 시작합니다. */
enum class YearTerm(val label: String, val months: String) {
    FIRST("1학기", "3–8월"),
    SECOND("2학기", "9–2월"),
    ALL_YEAR("1년 내내", "3–2월");

    /** [today] 가 속한 학교 한 해 안에서 이 때의 마지막 날. */
    fun endDate(today: LocalDate): LocalDate {
        val startYear = SchoolYear.of(today)
        return when (this) {
            FIRST -> SchoolYear.firstTermEnd(startYear)
            SECOND, ALL_YEAR -> SchoolYear.end(startYear)
        }
    }

    companion object {
        /** 지금의 학기. */
        fun current(today: LocalDate): YearTerm = if (SchoolYear.isFirstTerm(today)) FIRST else SECOND
    }
}
