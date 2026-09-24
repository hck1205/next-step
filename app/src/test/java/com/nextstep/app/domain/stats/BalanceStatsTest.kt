package com.nextstep.app.domain.stats

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
