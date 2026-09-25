package com.nextstep.app.domain.stats

import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.PeriodCalendar
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class BalanceStatsTest {
    private val today = DateUtils.today()

    @Test
    fun recommendedMinutesFollowStagePlanDefaults() {
        assertEquals(0, BalanceStats.recommendedWeekMinutes(null))
        assertEquals(0, BalanceStats.recommendedWeekMinutes(GrowthStage.NEWBORN))
        assertEquals(20 * 1 * 5, BalanceStats.recommendedWeekMinutes(GrowthStage.EARLY_ELEMENTARY))
        assertEquals(45 * 2 * 7, BalanceStats.recommendedWeekMinutes(GrowthStage.MIDDLE))
        assertEquals(120 * 2 * 5, BalanceStats.recommendedWeekMinutes(GrowthStage.GRADUATE))
    }

    @Test
    fun verdictNeverAsksForMoreAboveTheCeiling() {
        assertEquals(BalanceVerdict.NONE, BalanceStats.verdict(300, 0))
        assertEquals(BalanceVerdict.MORE, BalanceStats.verdict(40, 100))
        assertEquals(BalanceVerdict.WITHIN, BalanceStats.verdict(50, 100))
        assertEquals(BalanceVerdict.WITHIN, BalanceStats.verdict(120, 100))
        assertEquals(BalanceVerdict.LESS, BalanceStats.verdict(121, 100))
    }

    @Test
    fun selfDirectedRatioCountsStudentMadeTasksInWindow() {
        assertNull(BalanceStats.selfDirectedRatio(emptyList(), today))
        val tasks = listOf(
            Fixtures.task("a", today, by = "STUDENT"), Fixtures.task("b", today.plusDays(3), by = "PARENT"),
            Fixtures.task("c", today.minusDays(10), by = "STUDENT"), Fixtures.task("d", today.minusDays(60), by = "PARENT"),
            Fixtures.task("e", today, by = "STUDENT").copy(deleted = true),
        )
        assertEquals(2f / 3f, BalanceStats.selfDirectedRatio(tasks, today)!!, 0.0001f)
    }

    @Test
    fun youngestAgesGetAnOverheatingGuard() {
        // 영아·유아: 앉아서 하는 학습 권장선이 0 → 기록이 있으면 "줄이기"
        val baby = BalanceStats.report(GrowthStage.TODDLER, listOf(Fixtures.session("math", today, LocalTime.of(8, 0), 20)), emptyList(), emptyList(), null, today)
        assertEquals(BalanceVerdict.LESS, baby.studyVerdict); assertTrue(baby.studyLine.contains("권하지 않아요")); assertTrue(baby.headline.contains("줄여도"))
        // 단계를 모르면 판단하지 않음
        assertEquals(BalanceVerdict.NONE, BalanceStats.report(null, listOf(Fixtures.session("math", today, LocalTime.of(8, 0), 20)), emptyList(), emptyList(), null, today).studyVerdict)
        // 학원·수업 상한: 영아 1시간 · 유아 2시간 · 유치원기 5시간 · 학령기 없음
        assertEquals(listOf(60, 120, 300, null), listOf(GrowthStage.NEWBORN, GrowthStage.TODDLER, GrowthStage.PRESCHOOL, GrowthStage.EARLY_ELEMENTARY).map { BalanceStats.classCapWeekMinutes(it) })
        val monday = today.minusDays(today.dayOfWeek.value - 1L)
        val classes = listOf(
            Fixtures.event("영어 학원", monday, LocalTime.of(10, 0), LocalTime.of(13, 0), EventType.ACADEMY, weekly = true),
            Fixtures.event("발레", monday.plusDays(2), LocalTime.of(16, 0), LocalTime.of(19, 0), EventType.CLASS),
            Fixtures.event("병원", monday.plusDays(3), LocalTime.of(9, 0), LocalTime.of(11, 0), EventType.OTHER),
        )
        assertEquals(360, BalanceStats.classWeekMinutes(classes, today))
        val kid = BalanceStats.report(GrowthStage.PRESCHOOL, emptyList(), emptyList(), emptyList(), null, today, classes)
        assertEquals(BalanceVerdict.LESS, kid.classVerdict); assertTrue(kid.headline.startsWith("학원·수업이 많아요")); assertTrue(kid.classLine!!.contains("5시간"))
        val calm = BalanceStats.report(GrowthStage.PRESCHOOL, emptyList(), emptyList(), emptyList(), null, today, classes.take(1))
        assertEquals(BalanceVerdict.WITHIN, calm.classVerdict)
        val school = BalanceStats.report(GrowthStage.EARLY_ELEMENTARY, emptyList(), emptyList(), emptyList(), null, today, classes)
        assertEquals(BalanceVerdict.NONE, school.classVerdict); assertNull(school.classLine)
    }

    @Test
    fun reportCombinesStudyStreakAndExperiencesAndWritesSentences() {
        val born = today.minusYears(9)
        val period = PeriodCalendar.current(born, today)
        val sessions = listOf(Fixtures.session("math", today, LocalTime.of(8, 0), 30), Fixtures.session("math", today.minusDays(1), LocalTime.of(8, 0), 30))
        val activities = listOf(Fixtures.activity("과학관", ActivityType.FIELD_TRIP, today), Fixtures.activity("옛날", date = today.minusYears(3)))
        val r = BalanceStats.report(GrowthStage.EARLY_ELEMENTARY, sessions, emptyList(), activities, period, today)
        assertEquals(100, r.recommendedWeekMinutes); assertTrue(r.weekMinutes >= 30); assertEquals(2, r.streak)
        assertEquals(1, r.experiencesThisPeriod); assertNull(r.selfDirectedRatio)
        assertTrue(r.headline.isNotBlank()); assertTrue(r.studyLine.isNotBlank())
        val none = BalanceStats.report(GrowthStage.NEWBORN, emptyList(), emptyList(), emptyList(), null, today)
        assertEquals(BalanceVerdict.NONE, none.studyVerdict); assertTrue(none.headline.contains("놀이"))
        val over = BalanceReport(1000, 100, BalanceVerdict.LESS, null, 0, 0)
        assertTrue(over.headline.contains("쉬어도")); assertTrue(over.studyLine.contains("줄이는"))
        val steady = BalanceReport(80, 100, BalanceVerdict.WITHIN, 0.7f, 0, 4)
        assertTrue(steady.headline.contains("흐름"))
    }
}
