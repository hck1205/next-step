package com.nextstep.app.domain.year

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class YearTermTest {
    @Test
    fun currentTermFollowsKoreanSchoolYear() {
        assertEquals(YearTerm.FIRST, YearTerm.current(LocalDate.of(2029, 3, 2)))
        assertEquals(YearTerm.FIRST, YearTerm.current(LocalDate.of(2029, 8, 31)))
        assertEquals(YearTerm.SECOND, YearTerm.current(LocalDate.of(2029, 9, 1)))
        assertEquals(YearTerm.SECOND, YearTerm.current(LocalDate.of(2030, 2, 10)))
    }

    @Test
    fun endDateStaysInsideTheSchoolYear() {
        val autumn = LocalDate.of(2029, 10, 10)
        assertEquals(LocalDate.of(2029, 8, 31), YearTerm.FIRST.endDate(autumn))
        assertEquals(LocalDate.of(2030, 2, 28), YearTerm.SECOND.endDate(autumn))
        assertEquals(LocalDate.of(2030, 2, 28), YearTerm.ALL_YEAR.endDate(autumn))
        // 1~2월은 전 해에 시작한 학교 한 해: 2032년 2월은 윤년이라 29일.
        assertEquals(LocalDate.of(2032, 2, 29), YearTerm.ALL_YEAR.endDate(LocalDate.of(2032, 1, 5)))
        assertEquals(2031, YearTerm.schoolYearStart(LocalDate.of(2032, 1, 5)))
    }
}
