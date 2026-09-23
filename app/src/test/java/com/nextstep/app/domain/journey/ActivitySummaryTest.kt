package com.nextstep.app.domain.journey

import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ActivitySummaryTest {
    private val periods = PeriodCalendar.periods(LocalDate.of(2020, 5, 15))
    private val piano = Fixtures.activity("피아노", ActivityType.HOBBY, LocalDate.of(2028, 4, 1))
    private val club = Fixtures.activity("로봇반", ActivityType.CLUB, LocalDate.of(2029, 3, 5), LocalDate.of(2029, 8, 20))
    private val trip = Fixtures.activity("과학관", ActivityType.FIELD_TRIP, LocalDate.of(2029, 10, 1))
    private val gone = Fixtures.activity("삭제", ActivityType.TRAVEL, LocalDate.of(2029, 10, 2)).copy(deleted = true)
    private val all = listOf(trip, piano, club, gone)

    @Test
    fun liveDropsDeletedAndSortsNewestFirst() {
        assertEquals(listOf("과학관", "로봇반", "피아노"), ActivitySummary.live(all).map { it.title })
        assertEquals(mapOf(ActivityType.HOBBY to 1, ActivityType.CLUB to 1, ActivityType.FIELD_TRIP to 1), ActivitySummary.countByType(all))
    }

    @Test
    fun ongoingIsHobbyOrClubWithoutEndDate() {
        assertEquals(listOf("피아노"), ActivitySummary.ongoing(all).map { it.title })
        assertTrue(piano.isOngoing); assertFalse(club.isOngoing); assertFalse(trip.isOngoing)
        assertFalse(Fixtures.activity("x", ActivityType.HOBBY, end = LocalDate.of(2029, 1, 1)).isOngoing)
    }

    @Test
    fun byPeriodUsesStartDateAndClampsOutsideCalendar() {
        val grouped = ActivitySummary.byPeriod(all, periods)
        assertEquals(listOf("과학관"), grouped["g3s2"]!!.map { it.title }) // 2029-10 → 초3 2학기
        assertEquals(listOf("로봇반"), grouped["g3s1"]!!.map { it.title })
        assertEquals(listOf("피아노"), grouped["g2s1"]!!.map { it.title })
        assertTrue(grouped.values.flatten().none { it.deleted })
        val early = Fixtures.activity("출생 전", date = LocalDate.of(2019, 1, 1)); val late = Fixtures.activity("먼 미래", date = LocalDate.of(2090, 1, 1))
        val clamped = ActivitySummary.byPeriod(listOf(early, late), periods)
        assertEquals(listOf("출생 전"), clamped[periods.first().key]!!.map { it.title }); assertEquals(listOf("먼 미래"), clamped[periods.last().key]!!.map { it.title })
        assertTrue(ActivitySummary.byPeriod(all, emptyList()).isEmpty())
    }

    @Test
    fun countInPeriodAndDuration() {
        val g3s2 = periods.first { it.key == "g3s2" }
        assertEquals(1, ActivitySummary.countInPeriod(all, g3s2)); assertEquals(0, ActivitySummary.countInPeriod(all, null))
        val today = LocalDate.of(2029, 10, 10)
        assertEquals(1L, ActivitySummary.durationDays(trip, today))
        assertEquals(169L, ActivitySummary.durationDays(club, today))
        assertEquals(today.toEpochDay() - LocalDate.of(2028, 4, 1).toEpochDay() + 1, ActivitySummary.durationDays(piano, today)) // 진행 중 취미는 오늘까지
    }
}
