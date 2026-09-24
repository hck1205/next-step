package com.nextstep.app.domain.mission

import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.journey.GoalPlanner
import com.nextstep.app.domain.journey.PeriodCalendar
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MissionPlannerTest {
    private val periods = PeriodCalendar.periods(LocalDate.of(2015, 7, 3)) // 2026-03 초5
    private val today = LocalDate.of(2026, 4, 1)

    @Test
    fun enoughTimeKeepsDesignedOffsetsAndAssignsPeriods() {
        val target = today.plusDays(40)
        val (goal, steps) = MissionPlanner.create(MissionKind.PERFORMANCE, target, today, periods, "수학", "PARENT")
        assertEquals("수학 수행평가", goal.title); assertEquals(GoalArea.PERFORMANCE.name, goal.area); assertEquals(target.toEpochDay(), goal.targetDate)
        assertEquals(MissionKind.PERFORMANCE, MissionPlanner.kindOf(goal)); assertTrue(MissionPlanner.isMission(goal))
        assertEquals(MissionCatalog.of(MissionKind.PERFORMANCE).steps.map { target.minusDays(it.daysBefore.toLong()).toEpochDay() }, steps.map { it.dueDate })
        assertEquals((0 until steps.size).toList(), steps.map { it.orderIndex })
        assertTrue(steps.all { it.goalId == goal.id && it.periodKey == "g5s1" })
    }

    @Test
    fun shortNoticeCompressesEarlyStepsButNeverBeforeToday() {
        val target = today.plusDays(10)
        val due = MissionPlanner.schedule(MissionCatalog.of(MissionKind.EXAM), target, today).map { it.second }
        assertTrue(due.dropLast(1).all { !it.isBefore(today) && it.isBefore(target) })
        assertEquals(due.dropLast(1), due.dropLast(1).sorted())
        assertEquals(target.plusDays(3), due.last()) // 시험 뒤 단계는 그대로
        val sameDay = MissionPlanner.schedule(MissionCatalog.of(MissionKind.UNIT_TEST), today, today).map { it.second }
        assertEquals(listOf(today, today, today, today.plusDays(1)), sameDay)
    }

    @Test
    fun focusPicksNextOpenStepPerActiveMissionSoonestFirst() {
        val far = Fixtures.goal("수능", id = "far").copy(targetDate = today.plusDays(200).toEpochDay())
        val near = Fixtures.goal("수행", id = "near").copy(targetDate = today.plusDays(5).toEpochDay())
        val longTerm = Fixtures.goal("피아노", id = "long")
        val archived = Fixtures.goal("지난", id = "old", status = GoalStatus.ARCHIVED).copy(targetDate = today.toEpochDay())
        val steps = listOf(
            Fixtures.step("near", "g5s1", "a", id = "n0", order = 0, status = MilestoneStatus.DONE).copy(dueDate = today.minusDays(3).toEpochDay()),
            Fixtures.step("near", "g5s1", "b", id = "n1", order = 1).copy(dueDate = today.minusDays(1).toEpochDay()),
            Fixtures.step("near", "g5s1", "c", id = "n2", order = 2).copy(dueDate = today.plusDays(2).toEpochDay()),
            Fixtures.step("far", "g5s2", "x", id = "f0", order = 0).copy(dueDate = today.plusDays(30).toEpochDay()),
            Fixtures.step("long", "g5s1", "y", id = "l0"),
            Fixtures.step("old", "g5s1", "z", id = "o0"),
        )
        val focus = MissionPlanner.focus(listOf(far, longTerm, near, archived), steps, today)
        assertEquals(listOf("near", "far"), focus.map { it.goal.id })
        val n = focus.first()
        assertEquals("n1", n.nextStep.id); assertEquals(5, n.daysLeft); assertEquals(1, n.overdueSteps)
        assertEquals(3, n.stepCount); assertEquals(1, n.doneCount); assertEquals(1f / 3f, n.progress, 0.001f)
        assertNull(MissionPlanner.daysLeft(longTerm, today))
        assertEquals(1, MissionPlanner.focus(listOf(far, near), steps, today, limit = 1).size)
    }

    @Test
    fun missionWithAllStepsDoneDropsOutOfFocus() {
        val g = Fixtures.goal("끝", id = "g").copy(targetDate = today.toEpochDay())
        val s = listOf(Fixtures.step("g", "g5s1", "a", status = MilestoneStatus.DONE), Fixtures.step("g", "g5s1", "b", id = "b", order = 1, status = MilestoneStatus.SKIPPED))
        assertTrue(MissionPlanner.focus(listOf(g), s, today).isEmpty())
    }

    @Test
    fun taskForUsesStepDueDateAndGivenType() {
        val step = Fixtures.step("g", "g5s1", "오답 노트").copy(dueDate = today.plusDays(4).toEpochDay())
        val task = GoalPlanner.taskFor(step, "중간고사", null, today, "STUDENT", TaskType.EXAM_PREP)
        assertEquals(today.plusDays(4).toEpochDay(), task.dueDate); assertEquals(TaskType.EXAM_PREP, task.type)
        val late = GoalPlanner.taskFor(step.copy(dueDate = today.minusDays(2).toEpochDay()), "중간고사", null, today, "STUDENT")
        assertEquals(today.toEpochDay(), late.dueDate); assertEquals(TaskType.OTHER, late.type)
    }
}
