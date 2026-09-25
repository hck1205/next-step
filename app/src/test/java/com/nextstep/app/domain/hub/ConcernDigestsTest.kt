package com.nextstep.app.domain.hub

import com.nextstep.app.data.model.AptitudeDomain
import com.nextstep.app.domain.health.GrowthSignal
import com.nextstep.app.domain.health.GrowthSignalLevel
import com.nextstep.app.domain.health.GrowthSummary
import com.nextstep.app.domain.insight.AptitudeSignal
import com.nextstep.app.domain.mission.MissionFocus
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ConcernDigestsTest {
    private fun focus(title: String, daysLeft: Int, overdue: Int = 0) =
        MissionFocus(Fixtures.goal(title), null, Fixtures.step("g-$title", "g5s1", "다음"), daysLeft, overdue, 0.5f, 4, 2)

    @Test
    fun studyShowsWeekMinutesAndReviewedUnits() {
        val empty = ConcernDigests.study(0, emptyList())
        assertEquals(Concern.STUDY, empty.concern); assertEquals("이번 주 아직 0분", empty.headline); assertNull(empty.detail); assertTrue(empty.attention)
        val progress = SubjectProgress(Fixtures.math, total = 8, classCovered = 4, reviewed = 3, previewed = 0, previewQueue = emptyList(), reviewQueue = emptyList())
        val d = ConcernDigests.study(95, listOf(progress))
        assertEquals("이번 주 1시간 35분", d.headline); assertEquals("복습 3/8단원", d.detail); assertFalse(d.attention)
    }

    @Test
    fun examsShowNearestDDayAndRecentAverage() {
        assertEquals("다가오는 시험 없음", ConcernDigests.exams(emptyList(), emptyList()).headline)
        val grades = (1L..6L).map { Fixtures.grade("math", 40.0 + it * 10, it) }
        val d = ConcernDigests.exams(listOf(focus("수학 수행평가", 10), focus("국어 단원평가", 3, overdue = 1)), grades)
        assertEquals("국어 단원평가 D-3", d.headline); assertEquals("최근 5번 평균 80점", d.detail); assertTrue(d.attention)
        assertEquals("영어 D-day", ConcernDigests.exams(listOf(focus("영어", 0)), emptyList()).headline)
        assertFalse(ConcernDigests.exams(listOf(focus("영어", 0)), emptyList()).attention)
    }

    @Test
    fun growthShowsLatestHeightAndFlagsChecks() {
        val none = ConcernDigests.growth(null)
        assertEquals("아직 기록 없음", none.headline); assertFalse(none.attention)
        val summary = GrowthSummary(LocalDate.of(2029, 9, 1), 131.5, null, null, null, 5.0, null, null, listOf(GrowthSignal("시력 검진", "0.6", GrowthSignalLevel.CHECK)))
        val d = ConcernDigests.growth(summary)
        assertEquals("키 131.5cm", d.headline); assertEquals("1년에 5cm 속도", d.detail); assertTrue(d.attention)
    }

    @Test
    fun discoverShowsActivitiesAndTopSignal() {
        val none = ConcernDigests.discover(0, emptyList())
        assertEquals("이번 학기 활동 없음", none.headline); assertTrue(none.attention); assertNull(none.detail)
        val d = ConcernDigests.discover(2, listOf(AptitudeSignal(AptitudeDomain.MUSIC, 5, listOf("피아노"), 2, "정기 레슨")))
        assertEquals("이번 학기 활동 2개", d.headline); assertEquals("음악 쪽에 신호", d.detail); assertFalse(d.attention)
    }
}
