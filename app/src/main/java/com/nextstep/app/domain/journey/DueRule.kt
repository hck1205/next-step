package com.nextstep.app.domain.journey

import com.nextstep.app.domain.growth.GrowthStage
import java.time.LocalDate

/**
 * 이정표의 마감일을 생년월일로부터 계산하는 규칙. 한국의 행정·학제는 "출생연도 기준 몇 년 뒤 몇 월" 로 정해지는 것이 많아
 * 개월 수 규칙과 연도 규칙을 둘 다 둡니다.
 */
sealed interface DueRule {
    fun dueDate(birthDate: LocalDate): LocalDate

    /** 생후 [months]개월. 예: 4개월 영유아검진. */
    data class AgeMonths(val months: Int) : DueRule {
        override fun dueDate(birthDate: LocalDate): LocalDate = birthDate.plusMonths(months.toLong())
    }

    /** 출생연도 + [years]년의 [month]월 [day]일. 예: 유치원 지원은 "만 3세가 되는 해 11월" → BirthYearOffset(3, 11). */
    data class BirthYearOffset(val years: Int, val month: Int, val day: Int = 1) : DueRule {
        override fun dueDate(birthDate: LocalDate): LocalDate = LocalDate.of(birthDate.year + years, month, day)
    }

    /** 학년 [gradeYear](1=초1 … 18) 재학 중의 [month]월. 3월~12월은 그 학년도, 1~2월은 다음 해. */
    data class SchoolMonth(val gradeYear: Int, val month: Int, val day: Int = 1) : DueRule {
        override fun dueDate(birthDate: LocalDate): LocalDate {
            val entryYear = birthDate.year + GrowthStage.ELEMENTARY_ENTRY_YEARS_AFTER_BIRTH
            val year = entryYear + (gradeYear - 1) + if (month < SCHOOL_YEAR_START_MONTH) 1 else 0
            return LocalDate.of(year, month, day)
        }

        private companion object {
            const val SCHOOL_YEAR_START_MONTH = 3
        }
    }
}
