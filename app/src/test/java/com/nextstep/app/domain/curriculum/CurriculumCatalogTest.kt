package com.nextstep.app.domain.curriculum

import com.nextstep.app.domain.journey.PeriodCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CurriculumCatalogTest {
    private val calendarKeys = PeriodCalendar.periods(LocalDate.of(2015, 5, 1)).filter { it.isSchoolTerm }.map { it.key }

    @Test
    fun coversEveryElementaryToHighSchoolTermInCalendarOrder() {
        val expected = calendarKeys.filter { key -> key.removePrefix("g").substringBefore("s").toInt() <= 12 }
        assertEquals(expected, CurriculumCatalog.periodKeys)
        assertNull(CurriculumCatalog.forPeriod("g13s1")); assertNull(CurriculumCatalog.forPeriod("age-12")); assertNull(CurriculumCatalog.forPeriod(null))
    }

    @Test
    fun everyTermHasEssentialUnitsCompetenciesAndMathAndKorean() {
        CurriculumCatalog.periodKeys.forEach { key ->
            val t = CurriculumCatalog.forPeriod(key)!!
            assertTrue(key, t.units.any { it.essential }); assertTrue(key, t.competencies.isNotEmpty())
            assertTrue(key, t.subjects.contains("수학")); assertTrue(key, t.subjects.contains("국어"))
            t.units.forEach { u -> assertTrue("$key/${u.title}", u.title.isNotBlank() && u.matchTokens.isNotEmpty()) }
            assertEquals(key, t.units.map { it.subject + it.title }.size, t.units.map { it.subject + it.title }.toSet().size)
        }
    }

    @Test
    fun keyMilestonesFollowTheKoreanCurriculum() {
        assertTrue(CurriculumCatalog.forPeriod("g3s1")!!.subjects.containsAll(listOf("영어", "과학", "사회")))
        assertTrue(CurriculumCatalog.forPeriod("g2s2")!!.subjects.none { it == "영어" })
        assertTrue(CurriculumCatalog.forPeriod("g7s1")!!.unitsOf("수학").any { it.title.contains("정수와 유리수") && it.essential })
        assertTrue(CurriculumCatalog.forPeriod("g10s1")!!.unitsOf("수학").any { it.title.contains("공통수학1") })
        assertTrue(CurriculumCatalog.forPeriod("g11s1")!!.unitsOf("수학").any { it.title.contains("미적분I") && it.title.contains("미분") })
        assertTrue(CurriculumCatalog.forPeriod("g12s1")!!.unitsOf("수학").any { it.title.contains("미적분II") })
        assertTrue(CurriculumCatalog.forPeriod("g10s2")!!.startNow.any { it.contains("선택과목") })
    }
}
