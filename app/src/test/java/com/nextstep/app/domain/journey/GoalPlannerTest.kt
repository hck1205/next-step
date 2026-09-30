package com.nextstep.app.domain.journey

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalPlannerTest {
    private val periods = PeriodCalendar.periods(LocalDate.of(2024, 5, 15))
    private val track = GoalTrackCatalog.byId.getValue("math-elementary")

    @Test
    fun stepsForCopiesTrackStepsInOrderAndSkipsMissingPeriods() {
        val steps = GoalPlanner.stepsFor(track, periods, goalId = "g1", familyId = "fam")
        assertEquals(12, steps.size)
        assertEquals((0 until 12).toList(), steps.map { it.orderIndex })
        assertTrue(steps.all { it.goalId == "g1" && it.familyId == "fam" && it.status == MilestoneStatus.UPCOMING })
        assertEquals("g1s1", steps.first().periodKey); assertEquals(track.steps.first().title, steps.first().title)
        val onlyLate = periods.filter { it.isSchoolTerm && it.gradeYear!! >= 4 }
        assertEquals(6, GoalPlanner.stepsFor(track, onlyLate, "g1", "fam").size)
    }

    @Test
    fun relevantTracksNeedAStepInCurrentOrLaterPeriod() {
        val all = GoalTrackCatalog.tracks
        val atG7 = GoalPlanner.relevantTracks(all, periods, "g7s1").map { it.id }
        assertFalse("math-elementary" in atG7); assertTrue("math-middle" in atG7); assertTrue("math-high" in atG7)
        val newborn = GoalPlanner.relevantTracks(all, periods, "age-0").map { it.id }
        assertEquals(all.size, newborn.size)
        assertEquals(all.size, GoalPlanner.relevantTracks(all, periods, null).size)
        assertTrue(GoalPlanner.relevantTracks(all, emptyList(), null).isEmpty())
    }

    @Test
    fun progressIgnoresSkippedAndDeletedAndCompleteNeedsAllClosed() {
        val steps = listOf(
            Fixtures.step("g", "g1s1", "a", status = MilestoneStatus.DONE),
            Fixtures.step("g", "g1s2", "b", status = MilestoneStatus.SKIPPED),
            Fixtures.step("g", "g2s1", "c"),
            Fixtures.step("g", "g2s2", "d", status = MilestoneStatus.DONE).copy(deleted = true),
        )
        assertEquals(0.5f, GoalPlanner.progress(steps), 0.0001f)
        assertFalse(GoalPlanner.isComplete(steps))
        assertTrue(GoalPlanner.isComplete(steps.map { if (it.title == "c") it.copy(status = MilestoneStatus.DONE) else it }))
        assertFalse(GoalPlanner.isComplete(emptyList()))
        assertEquals(0f, GoalPlanner.progress(listOf(Fixtures.step("g", "g1s1", "x", status = MilestoneStatus.SKIPPED))), 0f)
    }

    @Test
    fun currentStepsIncludeCarriedOverUnfinishedStepsInPeriodOrder() {
        val steps = listOf(
            Fixtures.step("g", "g2s1", "now", order = 2),
            Fixtures.step("g", "g1s2", "late", order = 1),
            Fixtures.step("g", "g1s1", "done", order = 0, status = MilestoneStatus.DONE),
            Fixtures.step("g", "g2s2", "future", order = 3),
        )
        assertEquals(listOf("late", "now"), GoalPlanner.currentSteps(steps, periods, "g2s1").map { it.title })
        assertTrue(GoalPlanner.currentSteps(steps, periods, null).isEmpty())
    }

    @Test
    fun trackCustomAndAddedStepsBecomeGoalWithOrderedSteps() {
        val (goal, steps) = GoalPlanner.fromTrack(track, periods)
        assertEquals(track.id, goal.trackId); assertEquals(track.title, goal.title); assertEquals(12, steps.size)
        assertTrue(steps.all { it.goalId == goal.id && it.familyId == "" })
        val (mine, mySteps) = GoalPlanner.custom(" 줄넘기 100개 ", GoalArea.entries.first(), "", listOf("g3s1" to "50개", "g3s2" to " ", "g4s1" to "100개"))
        assertEquals("줄넘기 100개", mine.title); assertEquals(listOf("50개", "100개"), mySteps.map { it.title }); assertEquals(listOf(0, 1), mySteps.map { it.orderIndex })
        val added = GoalPlanner.step(mine.id, "g4s2", 2, " 이중 뛰기 ")
        assertEquals("이중 뛰기", added.title); assertEquals(2, added.orderIndex); assertEquals("g4s2", added.periodKey)
    }
}
