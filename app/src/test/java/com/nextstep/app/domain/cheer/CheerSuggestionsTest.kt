package com.nextstep.app.domain.cheer

import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class CheerSuggestionsTest {
    private val today = LocalDate.now()

    @Test
    fun emptyDayFallsBackToTwoLines() {
        val lines = CheerSuggestions.build(CheerSnapshot(), null)
        assertEquals(2, lines.size)
        assertTrue(lines.first().contains("수고했어")); assertTrue(lines.last().contains("언제든"))
    }

    @Test
    fun praisesMinutesStreakTasksAndTopSubject() {
        val lines = CheerSuggestions.build(CheerSnapshot(todayMinutes = 70, streak = 4, doneToday = 2, reviewedToday = 1, topSubjectName = "수학"), null)
        assertTrue(lines.any { it.contains("1시간 10분") })
        assertTrue(lines.any { it.contains("4일 연속") })
        assertTrue(lines.any { it.contains("2개 끝낸") })
        assertTrue(lines.any { it.contains("복습까지") })
        assertTrue(lines.any { it.contains("수학") })
        assertTrue(lines.last().contains("언제든"))
    }

    @Test
    fun wordingFollowsGrowthStage() {
        val young = CheerSuggestions.build(CheerSnapshot(doneToday = 1), GrowthStage.EARLY_ELEMENTARY)
        val high = CheerSuggestions.build(CheerSnapshot(doneToday = 1), GrowthStage.HIGH)
        assertTrue(young.first().contains("스스로")); assertTrue(high.first().contains("판단"))
    }

    @Test
    fun snapshotCountsOnlyToday() {
        val yesterday = today.minusDays(1)
        val sessions = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 40), Fixtures.session("eng", today, LocalTime.of(10, 0), 20), Fixtures.session("eng", yesterday, LocalTime.of(10, 0), 90))
        val tasks = listOf(Fixtures.task("done", today, done = true), Fixtures.task("open", today))
        val topics = listOf(Fixtures.topic("math", "a", 0, status = TopicStatus.REVIEWED), Fixtures.topic("math", "b", 1, status = TopicStatus.IN_CLASS))
        val s = CheerStats.snapshot(sessions, tasks, topics, listOf(Fixtures.math, Fixtures.english), today)
        assertEquals(60, s.todayMinutes); assertEquals(1, s.doneToday); assertEquals(1, s.reviewedToday); assertEquals("수학", s.topSubjectName)
        assertTrue(s.streak >= 1)
    }

    @Test
    fun noStudyTodayMeansNoTopSubject() {
        val s = CheerStats.snapshot(emptyList(), emptyList(), emptyList(), listOf(Fixtures.math), today)
        assertNull(s.topSubjectName); assertEquals(0, s.todayMinutes)
    }
}
