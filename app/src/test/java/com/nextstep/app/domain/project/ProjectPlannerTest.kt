package com.nextstep.app.domain.project

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProjectPlannerTest {
    private val english = ProjectCatalog.byId.getValue("english-reader")
    private val day = LocalDate.of(2029, 3, 5) // 월요일

    @Test
    fun ageFromBirthDateOrGrade() {
        assertEquals(40, ProjectPlanner.ageMonths(day.minusMonths(40), null, day))
        assertEquals(78, ProjectPlanner.ageMonths(null, 1, day))
        assertEquals(78 + 24 + 6, ProjectPlanner.ageMonths(null, 3, LocalDate.of(2029, 9, 1)))
        assertNull(ProjectPlanner.ageMonths(null, null, day))
    }

    @Test
    fun suggestedStartFollowsAge() {
        assertEquals(0, ProjectPlanner.suggestedStart(english, null))
        assertEquals(0, ProjectPlanner.suggestedStart(english, 20))
        assertEquals(1, ProjectPlanner.suggestedStart(english, 50))
        assertEquals(6, ProjectPlanner.suggestedStart(english, 105))
    }

    @Test
    fun recommendPutsNowBeforeLaterAndDropsStartedOrOutgrown() {
        val eightYearOld = 96
        val ids = ProjectPlanner.recommend(ProjectCatalog.plans, eightYearOld, started = setOf("piano")).map { it.id }
        assertFalse("piano" in ids)
        assertTrue(ids.indexOf("english-reader") < ids.indexOf("coding"))
        val teen = ProjectPlanner.recommend(ProjectCatalog.plans, 200, emptySet()).map { it.id }
        assertTrue(teen.isEmpty())
        assertEquals(ProjectCatalog.plans.size, ProjectPlanner.recommend(ProjectCatalog.plans, null, emptySet()).size)
    }

    @Test
    fun scheduleChainsPhasesFromTheStart() {
        val slots = ProjectPlanner.schedule(english, 4, day)
        assertNull(slots[3].start)
        assertEquals(day, slots[4].start)
        assertEquals(day.plusWeeks(52).minusDays(1), slots[4].end)
        assertEquals(slots[4].end!!.plusDays(1), slots[5].start)
    }

    @Test
    fun startSkipsEarlierPhasesAndKeepsMissionsSeparate() {
        val (goal, steps) = ProjectPlanner.start(english, 4, day, "PARENT")
        assertEquals("project:english-reader", goal.trackId)
        assertNull(goal.targetDate)
        assertEquals("LANGUAGE", goal.area)
        assertTrue(ProjectPlanner.isProject(goal))
        assertEquals(english, ProjectPlanner.planOf(goal))
        assertEquals(10, steps.size)
        assertEquals(List(4) { MilestoneStatus.SKIPPED }, steps.take(4).map { it.status })
        assertEquals(MilestoneStatus.IN_PROGRESS, steps[4].status)
        assertEquals("p5", ProjectPlanner.phaseKeyOf(steps[4]))
        assertTrue(steps.all { it.periodKey.startsWith("project:") && it.goalId == goal.id })
        assertNull(steps[0].dueDate)
    }

    @Test
    fun progressCountsThisWeekAndToday() {
        val (goal, steps) = saved(4)
        val logs = listOf(
            Fixtures.projectLog(goal.id, "p5", "파닉스 교재 한 쪽", 10, day.plusDays(2)),
            Fixtures.projectLog(goal.id, "p5", "흘려듣기", 15, day.plusDays(3)),
            Fixtures.projectLog(goal.id, "p5", "파닉스 교재 한 쪽", 10, day.plusDays(3)),
            Fixtures.projectLog(goal.id, "p5", "파닉스 교재 한 쪽", 30, day.minusDays(1)),
            Fixtures.projectLog("other", "p5", "파닉스 교재 한 쪽", 30, day.plusDays(3)),
        )
        val p = ProjectPlanner.progress(english, goal, steps, logs, day.plusDays(3))
        assertEquals(4, p.currentIndex)
        assertEquals("p5", p.current!!.key)
        assertEquals(0, p.passed)
        assertEquals(35, p.weekMinutes)
        assertEquals(english.phases[4].weeklyMinutes, p.weekTarget)
        assertEquals(25, p.todayMinutes)
        assertEquals(setOf("흘려듣기", "파닉스 교재 한 쪽"), p.todayDoneItems)
        assertEquals(2, p.activeDaysThisWeek)
        assertEquals(ProjectPace.ON_TRACK, p.pace)
        assertEquals(p.targetDate, p.projectedEnd)
        assertEquals(day.plusWeeks(52L * 6).minusDays(1), p.targetDate)
    }

    @Test
    fun passingEarlyIsAheadAndLateIsBehind() {
        val (goal, steps) = saved(4)
        val passed = steps.map { if (it.orderIndex == 4) it.copy(status = MilestoneStatus.DONE) else it }
        val early = ProjectPlanner.progress(english, goal, passed, emptyList(), day.plusDays(100))
        assertEquals(5, early.currentIndex)
        assertEquals(1, early.passed)
        assertEquals(ProjectPace.AHEAD, early.pace)
        assertTrue(early.projectedEnd!!.isBefore(early.targetDate))

        val due = LocalDate.ofEpochDay(steps[4].dueDate!!)
        val late = ProjectPlanner.progress(english, goal, steps, emptyList(), due.plusDays(10))
        assertEquals(ProjectPace.BEHIND, late.pace)
        assertEquals(late.targetDate!!.plusDays(10), late.projectedEnd)
    }

    @Test
    fun allPassedIsDone() {
        val (goal, steps) = saved(8)
        val done = steps.map { if (it.status == MilestoneStatus.SKIPPED) it else it.copy(status = MilestoneStatus.DONE) }
        val p = ProjectPlanner.progress(english, goal, done, emptyList(), day)
        assertTrue(p.isDone)
        assertEquals(ProjectPace.DONE, p.pace)
        assertNull(p.projectedEnd)
        assertEquals(2, p.passed)
    }

    @Test
    fun progressAllSkipsArchivedAndNonProjects() {
        val (goal, steps) = saved(0)
        val archived = goal.copy(id = "a", status = GoalStatus.ARCHIVED)
        val plain = Fixtures.goal("수학 목표")
        val list = ProjectPlanner.progressAll(listOf(goal, archived, plain), steps, emptyList(), day)
        assertEquals(listOf(goal.id), list.map { it.goalId })
    }

    @Test
    fun slotsFollowStoredDueDates() {
        val (goal, steps) = saved(4)
        val slots = ProjectPlanner.slotsOf(english, goal.copy(createdAt = 0L), steps)
        assertNull(slots[0].end)
        assertEquals(LocalDate.ofEpochDay(steps[4].dueDate!!), slots[4].end)
        assertEquals(slots[4].end!!.plusDays(1), slots[5].start)
    }

    private fun saved(from: Int): Pair<GoalEntity, List<GoalStepEntity>> {
        val (goal, steps) = ProjectPlanner.start(english, from, day, "PARENT")
        return goal.copy(familyId = Fixtures.FAMILY) to steps.map { it.copy(familyId = Fixtures.FAMILY) }
    }
}
