package com.nextstep.app.domain.mission

import com.nextstep.app.domain.growth.GrowthStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class MissionCatalogTest {
    @Test
    fun everyKindHasOrderedNonBlankSteps() {
        MissionKind.entries.forEach { kind ->
            val t = MissionCatalog.of(kind)
            assertTrue(kind.name, t.steps.size >= 3)
            assertTrue(kind.name, t.steps.all { it.title.isNotBlank() })
            assertEquals(kind.name, t.steps.map { it.daysBefore }.sortedDescending(), t.steps.map { it.daysBefore })
        }
    }

    @Test
    fun kindsFollowGrowthStage() {
        assertEquals(listOf(MissionKind.UNIT_TEST), MissionKind.forStage(GrowthStage.EARLY_ELEMENTARY))
        assertEquals(listOf(MissionKind.UNIT_TEST, MissionKind.PERFORMANCE), MissionKind.forStage(GrowthStage.UPPER_ELEMENTARY))
        val high = MissionKind.forStage(GrowthStage.HIGH)
        assertTrue(MissionKind.CSAT in high && MissionKind.EARLY_ADMISSION in high && MissionKind.CLUB in high && MissionKind.EXAM in high)
        assertFalse(MissionKind.CSAT in MissionKind.forStage(GrowthStage.MIDDLE))
        assertTrue(MissionKind.forStage(null).isEmpty())
    }

    @Test
    fun trackIdRoundTrips() {
        MissionKind.entries.forEach { assertEquals(it, MissionKind.ofTrackId(it.trackId)) }
        assertEquals(null, MissionKind.ofTrackId("math-elementary")); assertEquals(null, MissionKind.ofTrackId(null))
    }

    @Test
    fun titleIncludesSubjectOnlyWhenGiven() {
        assertEquals("수학 수행평가", MissionCatalog.of(MissionKind.PERFORMANCE).titleFor(" 수학 "))
        assertEquals("수능", MissionCatalog.of(MissionKind.CSAT).titleFor(""))
    }

    @Test
    fun suggestedDatesFollowKoreanCalendar() {
        val today = LocalDate.of(2031, 3, 2)
        val csat = MissionCatalog.suggestedDate(MissionKind.CSAT, today)
        assertEquals(11, csat.monthValue); assertEquals(DayOfWeek.THURSDAY, csat.dayOfWeek); assertTrue(csat.dayOfMonth in 8..14)
        val early = MissionCatalog.suggestedDate(MissionKind.EARLY_ADMISSION, today)
        assertEquals(9, early.monthValue); assertEquals(DayOfWeek.MONDAY, early.dayOfWeek)
        assertEquals(LocalDate.of(2031, 12, 20), MissionCatalog.suggestedDate(MissionKind.CLUB, today))
        assertEquals(today.plusDays(28), MissionCatalog.suggestedDate(MissionKind.EXAM, today))
        // 올해 수능이 지났으면 내년
        assertEquals(2032, MissionCatalog.suggestedDate(MissionKind.CSAT, LocalDate.of(2031, 12, 1)).year)
    }
}
