package com.nextstep.app.domain.stats

import com.nextstep.app.data.model.EventType
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class StudyStatsTest {
    private val today = DateUtils.today()
    private val subjects = listOf(Fixtures.math, Fixtures.english)

    @Test
    fun subjectScoresUsePercentAndTrendAndClassGap() {
        val grades = listOf(
            Fixtures.grade("math", 40.0, 1).copy(maxScore = 50.0),        // 80%
            Fixtures.grade("math", 90.0, 2, classAvg = 70.0),              // 90%, 반 평균 +20
        )
        val scores = StudyStats.subjectScores(grades, subjects)
        assertEquals(1, scores.size) // 영어는 성적이 없으면 제외
        val math = scores.single()
        assertEquals(85.0, math.average, 0.001)
        assertEquals(90.0, math.latest!!, 0.001)
        assertEquals(10.0, math.trend!!, 0.001)
        assertEquals(20.0, math.vsClass!!, 0.001)
        assertEquals(2, math.count)
    }

    @Test
    fun subjectScoreWithSingleGradeHasNoTrend() {
        val scores = StudyStats.subjectScores(listOf(Fixtures.grade("math", 70.0, 1)), subjects)
        assertNull(scores.single().trend)
        assertNull(scores.single().vsClass)
    }

    @Test
    fun subjectProgressComputesQueuesAndRatios() {
        val topics = Fixtures.topics("math", count = 6, covered = 4, reviewed = 1)
        val p = StudyStats.subjectProgress(topics, listOf(Fixtures.math), queueSize = 2).single()
        assertEquals(6, p.total); assertEquals(4, p.classCovered); assertEquals(1, p.reviewed)
        assertEquals(listOf("단원 1", "단원 2"), p.reviewQueue.map { it.title }) // 밀린 복습 3개 중 2개
        assertEquals(listOf("단원 4", "단원 5"), p.previewQueue.map { it.title })
        assertEquals(4f / 6f, p.classRatio, 0.001f)
        assertEquals(1f / 6f, p.myRatio, 0.001f)
    }

    @Test
    fun emptySubjectProgressHasZeroRatios() {
        val p = StudyStats.subjectProgress(emptyList(), listOf(Fixtures.math)).single()
        assertEquals(0f, p.classRatio, 0f); assertEquals(0f, p.myRatio, 0f)
    }

    @Test
    fun minutesAggregationsRespectDateWindows() {
        val sessions = listOf(
            Fixtures.session("math", today, LocalTime.of(9, 0), 30),
            Fixtures.session("math", today, LocalTime.of(20, 0), 40),
            Fixtures.session("eng", today.minusDays(1), LocalTime.of(20, 0), 50),
            Fixtures.session(null, today.minusDays(20), LocalTime.of(20, 0), 500),
        )
        assertEquals(70, StudyStats.todayMinutes(sessions))
        val daily = StudyStats.dailyMinutes(sessions, 3)
        assertEquals(listOf(0, 50, 70), daily.map { it.minutes })
        assertEquals(today, daily.last().date)
        val byHour = StudyStats.minutesByHour(sessions)
        assertEquals(30, byHour[9]); assertEquals(590, byHour[20])
    }

    @Test
    fun weeklyMinutesBySubjectAddsOtherBucketForUnknownSubjects() {
        val start = DateUtils.weekStart()
        val sessions = listOf(
            Fixtures.session("math", start, LocalTime.of(10, 0), 60),
            Fixtures.session("ghost", start, LocalTime.of(11, 0), 15),
            Fixtures.session(null, start, LocalTime.of(12, 0), 5),
        )
        val weekly = StudyStats.weeklyMinutesBySubject(sessions, subjects)
        assertEquals(60, weekly.first { it.subject?.id == "math" }.minutes)
        assertEquals(180, weekly.first { it.subject?.id == "math" }.goalMinutes)
        assertEquals(0, weekly.first { it.subject?.id == "eng" }.minutes)
        assertEquals(20, weekly.single { it.subject == null }.minutes)
    }

    @Test
    fun eventsOnExpandsWeeklyRepeatsOnlyAfterFirstOccurrence() {
        val first = today.minusWeeks(2)
        val weekly = Fixtures.event("수학", first, LocalTime.of(15, 0), LocalTime.of(16, 0), weekly = true)
        val single = Fixtures.event("시험", today, LocalTime.of(9, 0), LocalTime.of(10, 0), type = EventType.EXAM)
        val onToday = StudyStats.eventsOn(today, listOf(weekly, single))
        assertEquals(listOf("시험", "수학"), onToday.map { it.event.title }) // 시간순
        assertEquals(LocalTime.of(15, 0), DateUtils.toLocalDateTime(onToday[1].startAt).toLocalTime())
        assertTrue(StudyStats.eventsOn(first.minusWeeks(1), listOf(weekly)).isEmpty())
        assertTrue(StudyStats.eventsOn(today.plusDays(1), listOf(weekly, single)).isEmpty())
    }

    @Test
    fun upcomingExamsAreSortedAndWindowed() {
        val events = listOf(
            Fixtures.event("기말", today.plusDays(10), LocalTime.of(9, 0), LocalTime.of(10, 0), type = EventType.EXAM),
            Fixtures.event("쪽지", today.plusDays(2), LocalTime.of(9, 0), LocalTime.of(10, 0), type = EventType.EXAM),
            Fixtures.event("먼 시험", today.plusDays(40), LocalTime.of(9, 0), LocalTime.of(10, 0), type = EventType.EXAM),
            Fixtures.event("수업", today.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0)),
        )
        assertEquals(listOf("쪽지", "기말"), StudyStats.upcomingExams(events, emptyList()).map { it.title })
    }

    @Test
    fun taskFiltersSeparatePendingAndOverdue() {
        val tasks = listOf(
            Fixtures.task("어제", today.minusDays(1)),
            Fixtures.task("오늘", today),
            Fixtures.task("내일", today.plusDays(1)),
            Fixtures.task("끝", today.minusDays(3), done = true),
        )
        assertEquals(listOf("어제", "오늘"), StudyStats.pendingTasks(tasks).map { it.title })
        assertEquals(listOf("어제"), StudyStats.overdueTasks(tasks).map { it.title })
    }

    @Test
    fun studyStreakCountsConsecutiveDaysEndingTodayOrYesterday() {
        assertEquals(0, StudyStats.studyStreak(emptyList()))
        val threeDays = (0..2).map { Fixtures.session("math", today.minusDays(it.toLong()), LocalTime.of(20, 0), 10) }
        assertEquals(3, StudyStats.studyStreak(threeDays))
        val endingYesterday = (1..2).map { Fixtures.session("math", today.minusDays(it.toLong()), LocalTime.of(20, 0), 10) }
        assertEquals(2, StudyStats.studyStreak(endingYesterday))
        val broken = listOf(today, today.minusDays(2)).map { Fixtures.session("math", it, LocalTime.of(20, 0), 10) }
        assertEquals(1, StudyStats.studyStreak(broken))
    }
}
