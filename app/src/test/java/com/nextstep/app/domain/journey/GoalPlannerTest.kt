package com.nextstep.app.domain.journey

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

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
    fun taskForUsesPeriodEndOrFallbackAndKeepsContextInNote() {
        val step = Fixtures.step("g", "g3s1", "나눗셈 개념").copy(detail = "곱셈과의 관계")
        val g3s1 = periods.first { it.key == "g3s1" }
        val task = GoalPlanner.taskFor(step, "초등 수학", g3s1, g3s1.start.plusDays(10), "PARENT")
        assertEquals(g3s1.end.toEpochDay(), task.dueDate); assertEquals("나눗셈 개념", task.title); assertEquals(TaskType.OTHER, task.type)
        assertEquals("초등 수학 · 곱셈과의 관계", task.note); assertEquals("PARENT", task.createdByRole); assertEquals("", task.familyId)
        val today = g3s1.end.plusDays(30)
        assertEquals(today.plusDays(7).toEpochDay(), GoalPlanner.taskFor(step, "", g3s1, today, "STUDENT").dueDate)
        assertEquals(today.plusDays(7).toEpochDay(), GoalPlanner.taskFor(step, "", null, today, "STUDENT").dueDate)
    }
}
