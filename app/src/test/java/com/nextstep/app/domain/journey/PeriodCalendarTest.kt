package com.nextstep.app.domain.journey

import com.nextstep.app.domain.growth.GrowthStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PeriodCalendarTest {
    private val born = LocalDate.of(2024, 5, 15)
    private val periods = PeriodCalendar.periods(born)

    @Test
    fun periodsAreContiguousAndCoverBirthToGraduateSchool() {
        assertEquals(born, periods.first().start)
        periods.zipWithNext().forEach { (a, b) -> assertEquals("${a.key}→${b.key}", a.end.plusDays(1), b.start) }
        assertEquals(JourneyPeriod.termKey(18, 2), periods.last().key)
        assertEquals(periods.map { it.key }.size, periods.map { it.key }.toSet().size)
    }

    @Test
    fun newbornQuartersThenHalfYearsUntilSchoolEntry() {
        assertEquals(listOf("age-0", "age-3", "age-6", "age-9", "age-12", "age-18"), periods.take(6).map { it.key })
        assertEquals("생후 0~2개월", periods[0].label); assertEquals("만 1세 전반", periods[4].label); assertEquals("만 1세 후반", periods[5].label)
        assertEquals(GrowthStage.NEWBORN, periods[0].stage); assertEquals(GrowthStage.TODDLER, periods[4].stage)
        val entry = PeriodCalendar.entryDate(born)
        assertEquals(LocalDate.of(2031, 3, 1), entry)
        val lastPre = periods.last { !it.isSchoolTerm }
        assertEquals(entry.minusDays(1), lastPre.end)
        assertEquals(GrowthStage.PRESCHOOL, lastPre.stage)
    }

    @Test
    fun schoolTermsFollowMarchAndSeptemberWithLeapFebruary() {
        val g1s1 = periods.first { it.key == "g1s1" }
        assertEquals(LocalDate.of(2031, 3, 1), g1s1.start); assertEquals(LocalDate.of(2031, 8, 31), g1s1.end)
        assertEquals("초1 1학기", g1s1.label); assertEquals(1, g1s1.gradeYear); assertEquals(1, g1s1.semester)
        val g1s2 = periods.first { it.key == "g1s2" }
        assertEquals(LocalDate.of(2031, 9, 1), g1s2.start); assertEquals(LocalDate.of(2032, 2, 29), g1s2.end)
        assertEquals("중1 2학기", periods.first { it.key == "g7s2" }.label)
        assertEquals("대학원 1년차 1학기", periods.first { it.key == "g17s1" }.label)
        assertEquals(36, periods.count { it.isSchoolTerm })
    }

    @Test
    fun currentAndPeriodOfResolveByDate() {
        assertEquals("age-3", PeriodCalendar.current(born, LocalDate.of(2024, 8, 20))!!.key)
        assertEquals("g3s2", PeriodCalendar.current(born, LocalDate.of(2034, 1, 10))!!.key)
        assertNull(PeriodCalendar.current(born, LocalDate.of(2024, 1, 1)))
        assertNull(PeriodCalendar.current(born, LocalDate.of(2060, 1, 1)))
        assertTrue(LocalDate.of(2031, 3, 1) in periods.first { it.key == "g1s1" })
        assertEquals("g1s1", PeriodCalendar.periodOf(periods, LocalDate.of(2031, 3, 1))!!.key)
    }

    @Test
    fun januaryBirthGetsShorterLastPreschoolPeriod() {
        val jan = PeriodCalendar.periods(LocalDate.of(2020, 1, 20))
        val entry = PeriodCalendar.entryDate(LocalDate.of(2020, 1, 20))
        assertEquals(LocalDate.of(2027, 3, 1), entry)
        val lastPre = jan.last { !it.isSchoolTerm }
        assertTrue(lastPre.end.isBefore(lastPre.start.plusMonths(6)))
        assertEquals(entry.minusDays(1), lastPre.end)
    }
}
