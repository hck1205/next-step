package com.nextstep.app.domain.stats

import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class StudyHabitsTest {
    private val today = LocalDate.of(2029, 10, 10)
    private fun s(daysAgo: Long, hour: Int, minutes: Int) = Fixtures.session("math", today.minusDays(daysAgo), LocalTime.of(hour, 0), minutes)

    @Test
    fun readsWhenHowLongAndHowSteadilyWithinFourWeeks() {
        val r = StudyHabits.report(listOf(s(0, 20, 30), s(1, 20, 60), s(2, 8, 20), s(4, 15, 100), s(10, 21, 40), s(40, 20, 50)), today)
        assertEquals(250, r.totalMinutes); assertEquals(5, r.activeDays); assertEquals(5, r.sessionCount)
        assertEquals(50, r.averageSessionMinutes); assertEquals(100, r.longestSessionMinutes)
        assertEquals(mapOf(DayPart.MORNING to 20, DayPart.AFTERNOON to 100, DayPart.EVENING to 130, DayPart.NIGHT to 0), r.byPart)
        assertEquals(DayPart.EVENING, r.bestPart)
        assertEquals(3, r.currentStreak); assertEquals(3, r.longestStreak)
        assertEquals(210, r.thisWeekMinutes); assertEquals(40, r.lastWeekMinutes)
        assertEquals(250, r.byWeekday.values.sum())
        assertEquals("저녁에 가장 많이 공부해요(2시간 10분).", r.lines[0])
        assertTrue(r.lines[1].contains("100분") && r.lines[1].contains("쉬어 가요"))
        assertEquals("28일 중 5일 공부했어요 · 지금 3일째 이어 가는 중.", r.lines[2])
        assertEquals("지난 7일은 그 전 7일보다 425% 더 했어요.", r.lines[3])
    }

    @Test
    fun streakCountsFromYesterdayWhenTodayIsNotDoneYet() {
        val r = StudyHabits.report(listOf(s(1, 9, 20), s(2, 9, 20), s(5, 9, 20)), today)
        assertEquals(2, r.currentStreak); assertEquals(2, r.longestStreak)
        assertEquals("지난주보다 더 했어요.", r.lines.last())
    }

    @Test
    fun emptyHistoryGivesOneGentleLine() {
        val r = StudyHabits.report(emptyList(), today)
        assertTrue(r.isEmpty); assertEquals(null, r.bestPart)
        assertEquals(listOf("최근 4주 동안 공부 기록이 없어요. 타이머로 한 번 재 보면 습관이 보여요."), r.lines)
    }

    @Test
    fun dayPartsSplitTheClock() {
        assertEquals(DayPart.NIGHT, DayPart.of(4)); assertEquals(DayPart.MORNING, DayPart.of(5))
        assertEquals(DayPart.AFTERNOON, DayPart.of(12)); assertEquals(DayPart.EVENING, DayPart.of(21)); assertEquals(DayPart.NIGHT, DayPart.of(23))
    }
}
