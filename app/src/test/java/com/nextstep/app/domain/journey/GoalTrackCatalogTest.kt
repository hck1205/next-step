package com.nextstep.app.domain.journey

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalTrackCatalogTest {
    private val validKeys = PeriodCalendar.periods(LocalDate.of(2024, 5, 15)).map { it.key }
    private val order = validKeys.withIndex().associate { it.value to it.index }

    @Test
    fun idsUniqueAndEveryTrackHasStepsWithTexts() {
        val ids = GoalTrackCatalog.tracks.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(GoalTrackCatalog.tracks.size, GoalTrackCatalog.byId.size)
        GoalTrackCatalog.tracks.forEach { t ->
            assertTrue(t.id, t.steps.size >= 4)
            assertTrue(t.id, t.title.isNotBlank() && t.description.isNotBlank())
            t.steps.forEach { s -> assertTrue("${t.id}/${s.periodKey}", s.title.isNotBlank() && s.detail.isNotBlank()) }
        }
    }

    @Test
    fun stepPeriodKeysExistInCalendarAndAscendWithOnePerPeriod() {
        GoalTrackCatalog.tracks.forEach { t ->
            val keys = t.steps.map { it.periodKey }
            keys.forEach { assertTrue("${t.id}/$it", it in order) }
            assertEquals(t.id, keys, keys.sortedBy { order.getValue(it) })
            assertEquals(t.id, keys.size, keys.toSet().size)
        }
    }

    @Test
    fun elementaryTracksCoverAllTwelveTermsAndAreasAreSpread() {
        listOf("math-elementary", "korean-elementary", "habits-elementary", "experience-elementary").forEach { id ->
            assertEquals(id, (1..6).flatMap { g -> listOf("g${g}s1", "g${g}s2") }, GoalTrackCatalog.byId.getValue(id).steps.map { it.periodKey })
        }
        assertTrue(GoalTrackCatalog.tracks.map { it.area }.toSet().containsAll(listOf(GoalArea.LANGUAGE, GoalArea.MATH, GoalArea.KOREAN, GoalArea.HABIT, GoalArea.EXPERIENCE, GoalArea.CAREER)))
        assertEquals("age-36", GoalTrackCatalog.byId.getValue("english-early").firstPeriodKey)
        assertEquals("g6s2", GoalTrackCatalog.byId.getValue("english-early").lastPeriodKey)
    }
}
