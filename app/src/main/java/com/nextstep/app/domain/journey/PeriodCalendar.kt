package com.nextstep.app.domain.journey

import com.nextstep.app.domain.growth.GrowthStage
import java.time.LocalDate

/**
 * 생년월일 하나로 출생부터 대학원까지의 구간 목록을 만듭니다. 순수 함수라 화면·계획·테스트가 같은 달력을 봅니다.
 * 학령기 구간은 학제 날짜(3월 1일 입학)로, 학령 전 구간은 개월 수로 만들고 입학 전날에서 잘라 냅니다.
 */
object PeriodCalendar {
    private const val NEWBORN_STEP_MONTHS = 3
    private const val PRESCHOOL_STEP_MONTHS = 6
    private const val FIRST_HALF_YEAR_MONTHS = 12

    fun periods(birthDate: LocalDate): List<JourneyPeriod> {
        val entry = entryDate(birthDate)
        val result = mutableListOf<JourneyPeriod>()
        var months = 0
        while (true) {
            val step = if (months < FIRST_HALF_YEAR_MONTHS) NEWBORN_STEP_MONTHS else PRESCHOOL_STEP_MONTHS
            val start = birthDate.plusMonths(months.toLong())
            if (!start.isBefore(entry)) break
            val end = minOf(birthDate.plusMonths((months + step).toLong()).minusDays(1), entry.minusDays(1))
            val stage = GrowthStage.fromAgeMonths(months) ?: GrowthStage.PRESCHOOL
            result += JourneyPeriod(JourneyPeriod.ageKey(months), ageLabel(months, step), stage, start, end)
            months += step
        }
        for (grade in GrowthStage.MIN_GRADE..GrowthStage.MAX_GRADE) {
            val stage = GrowthStage.fromGradeYear(grade) ?: continue
            val year = entry.year + grade - 1
            result += JourneyPeriod(JourneyPeriod.termKey(grade, 1), "${stage.gradeLabel(grade)} 1학기", stage, LocalDate.of(year, 3, 1), LocalDate.of(year, 8, 31), grade, 1)
            result += JourneyPeriod(JourneyPeriod.termKey(grade, 2), "${stage.gradeLabel(grade)} 2학기", stage, LocalDate.of(year, 9, 1), LocalDate.of(year + 1, 2, 28).let { if (it.isLeapYear) it.plusDays(1) else it }, grade, 2)
        }
        return result
    }

    /** 오늘이 속한 구간. 출생 전이거나 대학원 이후면 null. */
    fun current(birthDate: LocalDate, today: LocalDate): JourneyPeriod? = periods(birthDate).firstOrNull { today in it }

    fun periodOf(periods: List<JourneyPeriod>, date: LocalDate): JourneyPeriod? = periods.firstOrNull { date in it }

    /** 초등 입학일: 출생연도 + 7년의 3월 1일. */
    fun entryDate(birthDate: LocalDate): LocalDate = LocalDate.of(birthDate.year + GrowthStage.ELEMENTARY_ENTRY_YEARS_AFTER_BIRTH, 3, 1)

    private fun ageLabel(startMonths: Int, step: Int): String = when {
        startMonths < FIRST_HALF_YEAR_MONTHS -> "생후 ${startMonths}~${startMonths + step - 1}개월"
        startMonths % 12 == 0 -> "만 ${startMonths / 12}세 전반"
        else -> "만 ${startMonths / 12}세 후반"
    }
}
