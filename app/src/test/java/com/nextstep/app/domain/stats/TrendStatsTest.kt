package com.nextstep.app.domain.stats

import com.nextstep.app.data.model.Role
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrendStatsTest {
    private val today = LocalDate.of(2026, 4, 14) // 화요일
    private val four = LocalTime.of(16, 0)

    @Test
    fun rollingWeeksSumSevenDayWindowsEndingToday() {
        val sessions = listOf(
            Fixtures.session("math", today, four, 30),
            Fixtures.session("math", today.minusDays(6), four, 20),
            Fixtures.session("eng", today.minusDays(7), four, 40),
            Fixtures.session("eng", today.minusDays(60), four, 99),
        )
        val weeks = TrendStats.rollingWeeks(sessions, today)
        assertEquals(8, weeks.size)
        assertEquals(50, weeks.last()) // 오늘부터 6일 전까지
        assertEquals(40, weeks[6]) // 그 앞 7일
        assertEquals(0, weeks.first()) // 60일 전은 8주 밖
    }

    @Test
    fun heatCalendarStartsOnMondayAndMarksFutureDays() {
        val sessions = listOf(Fixtures.session("math", today, four, 50), Fixtures.session("math", today.minusDays(1), four, 10))
        val weeks = TrendStats.heatCalendar(sessions, today)
        assertEquals(5, weeks.size)
        assertEquals(LocalDate.of(2026, 3, 16), weeks.first().monday)
        val now = weeks.last()
        assertEquals(LocalDate.of(2026, 4, 13), now.monday)
        assertEquals(listOf(1, 4), now.days.take(2).map { it.level })
        assertTrue(now.days.drop(2).all { it.future && it.minutes == 0 })
        assertEquals(2, now.activeDays); assertEquals(60, now.totalMinutes)
    }

    @Test
    fun heatLevelsStepEveryFifteenMinutes() {
        assertEquals(listOf(0, 1, 2, 3, 4, 4), listOf(0, 14, 15, 30, 45, 200).map(TrendStats::heatLevel))
    }

    @Test
    fun scoreSeriesIsDateOrderedPercentWithClassAverage() {
        val grades = listOf(Fixtures.grade("math", 90.0, 20, classAvg = 70.0), Fixtures.grade("math", 80.0, 10, classAvg = 80.0))
        val series = TrendStats.scoreSeries(grades, listOf(Fixtures.math, Fixtures.english))
        val math = series.single()
        assertEquals(listOf(80, 90), math.percents); assertEquals(75, math.classAverage)
        assertEquals(90, math.last); assertEquals(10, math.change)
        assertNull(TrendStats.scoreSeries(listOf(Fixtures.grade("eng", 70.0, 1)), listOf(Fixtures.english)).single().change)
    }

    @Test
    fun submissionsSplitDonePendingAndLate() {
        val tasks = listOf(
            Fixtures.task("done", today.minusDays(3), by = Role.MENTOR.name, done = true),
            Fixtures.task("late", today.minusDays(1), by = Role.MENTOR.name),
            Fixtures.task("soon", today.plusDays(2), by = Role.MENTOR.name),
            Fixtures.task("mine", today.minusDays(1), by = Role.STUDENT.name),
        )
        val s = TrendStats.submissions(tasks, today)
        assertEquals(Submissions(done = 1, pending = 1, late = 1), s); assertEquals(3, s.total)
    }

    @Test
    fun familyBundlesEveryChartValueForOneDay() {
        val sessions = listOf(Fixtures.session("math", today, four, 30), Fixtures.session("math", today.minusDays(8), four, 20))
        val tasks = listOf(Fixtures.task("a", today, by = Role.STUDENT.name), Fixtures.task("b", today, by = Role.MENTOR.name, done = true))
        val grades = listOf(Fixtures.grade("math", 70.0, 1), Fixtures.grade("math", 80.0, 2))
        val t = TrendStats.family(sessions, tasks, grades, listOf(Fixtures.math, Fixtures.english), today)
        assertEquals(30, t.recent); assertEquals(10, t.recentChange); assertTrue(t.hasStudy); assertTrue(t.hasTasks)
        assertEquals(7, t.daily.size); assertEquals(today, t.daily.last().date); assertEquals(30, t.daily.last().minutes)
        assertEquals(listOf(30, 0), t.bySubject.map { it.minutes }) // 이번 주(월요일부터)만
        assertEquals(80, t.scoreAverage); assertEquals(10, t.scoreChange)
        assertEquals(0.5f, t.selfShare, 0.001f); assertEquals(1, t.submissions.done)
        assertEquals(listOf("STUDENT", "MENTOR"), t.assigners.map { it.key }) // 기록 탭과 같은 PlanHistory 계산
        assertEquals(5, t.weekRates.size); assertEquals(50, t.weekRates.last().percent)
        assertEquals((180 + 180) / 7, t.dailyGoal)
        assertNull(FamilyTrends().recentChange); assertNull(FamilyTrends().scoreAverage)
    }
}
