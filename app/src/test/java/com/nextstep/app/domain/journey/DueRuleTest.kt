package com.nextstep.app.domain.journey

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DueRuleTest {
    private val born = LocalDate.of(2024, 5, 15)

    @Test
    fun ageMonthsAddsToBirthDate() {
        assertEquals(LocalDate.of(2024, 7, 15), DueRule.AgeMonths(2).dueDate(born))
        assertEquals(LocalDate.of(2025, 5, 15), DueRule.AgeMonths(12).dueDate(born))
    }

    @Test
    fun birthYearOffsetIgnoresBirthMonthAndDay() {
        // 만 3세가 되는 해(2027) 11월 유치원 지원
        assertEquals(LocalDate.of(2027, 11, 1), DueRule.BirthYearOffset(3, 11).dueDate(born))
        assertEquals(LocalDate.of(2031, 1, 10), DueRule.BirthYearOffset(7, 1, 10).dueDate(born))
    }

    @Test
    fun schoolMonthUsesEntryYearAndRollsWinterMonthsToNextYear() {
        // 2024년생 → 2031년 3월 초1 입학. 초1 9월 = 2031-09, 초1 1월 = 2032-01, 고3(12학년) 9월 = 2042-09
        assertEquals(LocalDate.of(2031, 9, 1), DueRule.SchoolMonth(1, 9).dueDate(born))
        assertEquals(LocalDate.of(2032, 1, 1), DueRule.SchoolMonth(1, 1).dueDate(born))
        assertEquals(LocalDate.of(2032, 2, 1), DueRule.SchoolMonth(1, 2).dueDate(born))
        assertEquals(LocalDate.of(2042, 9, 5), DueRule.SchoolMonth(12, 9, 5).dueDate(born))
        assertEquals(LocalDate.of(2043, 8, 1), DueRule.SchoolMonth(13, 8).dueDate(born))
    }
}
