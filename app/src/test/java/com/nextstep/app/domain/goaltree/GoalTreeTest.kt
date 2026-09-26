package com.nextstep.app.domain.goaltree

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.mission.MissionPlanner
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class GoalTreeTest {
    private val utc = ZoneOffset.UTC
    private val today = LocalDate.of(2029, 5, 10)
    private val millis = { d: LocalDate -> d.atStartOfDay(utc).toInstant().toEpochMilli() }

    private fun goal(id: String, leadsTo: String? = null, status: GoalStatus = GoalStatus.ACTIVE, target: LocalDate? = null) = GoalEntity(
        id = id, familyId = Fixtures.FAMILY, trackId = GoalTree.TRACK, title = "목표$id", area = GoalArea.LANGUAGE.name, leadsTo = leadsTo, status = status,
        targetDate = target?.toEpochDay(), createdAt = millis(today.minusDays(20)),
    )

    private fun task(goalId: String, title: String, due: LocalDate, done: LocalDate? = null) = TaskEntity(
        id = "t-$title", familyId = Fixtures.FAMILY, title = title, dueDate = due.toEpochDay(), createdByRole = "PARENT", goalId = goalId,
        done = done != null, doneAt = done?.let(millis),
    )

    @Test
    fun createAndSubTaskCarryTheGoal() {
        val g = GoalTree.create(" 영어 일기 ", " 쓰기 자신감 ", GoalArea.LANGUAGE, today.plusDays(30), "big", "PARENT")
        assertTrue(GoalTree.isTreeGoal(g)); assertEquals("영어 일기", g.title); assertEquals("쓰기 자신감", g.description); assertEquals("big", g.leadsTo)
        assertFalse(MissionPlanner.isMission(g)) // 기한이 있어도 시험·목표가 아님
        val t = GoalTree.subTask(g, " 3줄 쓰기 ", today, "eng", TaskType.HOMEWORK, "MENTOR")
        assertEquals(g.id, t.goalId); assertEquals("영어 일기", t.note); assertEquals("MENTOR", t.createdByRole); assertEquals("3줄 쓰기", t.title)
    }

    @Test
    fun rateCountsTasksAndAchievedSmallerGoals() {
        val goals = listOf(goal("big"), goal("a", leadsTo = "big", status = GoalStatus.DONE), goal("b", leadsTo = "big"))
        val tasks = listOf(task("big", "x", today.minusDays(2), done = today.minusDays(3)), task("big", "y", today.minusDays(1)), task("a", "z", today.plusDays(1)))
        val n = GoalTree.node(goals[0], goals, tasks, today, utc)
        assertEquals(2, n.totalTasks); assertEquals(1, n.doneTasks); assertEquals(2, n.children.size); assertEquals(1, n.achievedChildren)
        assertEquals(0.5f, n.rate, 0.001f) // (1 + 1) / (2 + 2)
        assertEquals(1, n.overdue); assertEquals(today.minusDays(3), n.lastActivity); assertEquals(3, n.idleDays)
        assertEquals("y", n.pending.single().title)
        assertFalse(n.readyToAchieve)
        val empty = GoalTree.node(goals[2], goals, tasks, today, utc)
        assertEquals(0f, empty.rate, 0f); assertFalse(empty.readyToAchieve); assertEquals(20, empty.idleDays)
    }

    @Test
    fun readyWhenEverythingUnderItIsDone() {
        val goals = listOf(goal("a", target = today.plusDays(5)))
        val n = GoalTree.node(goals[0], goals, listOf(task("a", "x", today, done = today)), today, utc)
        assertTrue(n.readyToAchieve); assertEquals(5, n.daysLeft); assertEquals(0, n.idleDays)
        assertFalse(GoalTree.node(goals[0].copy(status = GoalStatus.DONE), goals, emptyList(), today, utc).readyToAchieve)
    }

    @Test
    fun chainsGoUpwardAndNeverLoop() {
        val goals = listOf(goal("c", leadsTo = "b"), goal("b", leadsTo = "a"), goal("a", leadsTo = "c"))
        assertEquals(listOf("b", "a"), GoalTree.chain(goals[0], goals).map { it.id })
        val line = listOf(goal("top"), goal("mid", leadsTo = "top"), goal("leaf", leadsTo = "mid"), goal("other"))
        assertEquals(setOf("mid", "leaf"), GoalTree.descendants("top", line))
        assertEquals(listOf("other"), GoalTree.linkTargets(line[0], line).map { it.id })
        assertEquals(listOf("top", "other"), GoalTree.linkTargets(line[1], line).map { it.id })
        assertEquals(4, GoalTree.linkTargets(null, line).size)
        val nodes = GoalTree.nodes(line, emptyList(), today, utc)
        assertEquals(listOf("top", "other"), GoalTree.roots(nodes).map { it.goal.id })
        assertEquals("mid", nodes.first { it.goal.id == "leaf" }.parent!!.id)
    }

    @Test
    fun achievingASmallGoalRaisesTheNextOne() {
        val goals = listOf(goal("big"), goal("small", leadsTo = "big", status = GoalStatus.DONE))
        val nodes = GoalTree.nodes(goals, listOf(task("big", "x", today)), today, utc)
        assertEquals("→ 목표big 50%", GoalTree.nextStepLine(nodes.first { it.goal.id == "small" }, nodes))
        assertNull(GoalTree.nextStepLine(nodes.first { it.goal.id == "big" }, nodes))
    }

    @Test
    fun focusPutsOverdueThenReadyThenStalledFirst() {
        val goals = listOf(goal("idle"), goal("ready"), goal("late"), goal("done", status = GoalStatus.DONE), goal("fresh"))
        val tasks = listOf(
            task("ready", "r", today, done = today), task("late", "l", today.minusDays(1)), task("fresh", "f", today.plusDays(1)),
        )
        val nodes = GoalTree.nodes(goals, tasks, today, utc)
        assertEquals(listOf("late", "ready", "idle"), GoalTree.focus(nodes).map { it.goal.id })
    }

    @Test
    fun attentionPicksOneThingToLookAtFirst() {
        val goals = listOf(goal("ready"), goal("late"), goal("idle"), goal("empty"), goal("done", status = GoalStatus.DONE))
        val tasks = listOf(
            task("ready", "r", today, done = today), task("late", "l", today.minusDays(1)), task("idle", "i", today.plusDays(1)),
            task("done", "d", today.minusDays(3)),
        )
        val fresh = goals.map { if (it.id == "empty") it.copy(createdAt = millis(today)) else it }
        val byId = GoalTree.nodes(fresh, tasks, today, utc).associateBy { it.goal.id }
        assertEquals(GoalAttention.READY, byId.getValue("ready").attention)
        assertEquals("밀린 할 일 1개", byId.getValue("late").attentionLine)
        assertEquals(GoalAttention.IDLE, byId.getValue("idle").attention); assertTrue(byId.getValue("idle").isIdle)
        assertEquals("20일째 그대로예요", byId.getValue("idle").attentionLine)
        assertEquals(GoalAttention.EMPTY, byId.getValue("empty").attention)
        assertNull(byId.getValue("done").attention) // 달성한 목표는 밀린 할 일이 있어도 조용히
        assertFalse(byId.getValue("done").isIdle)
    }

    @Test
    fun assignerNamesWhoGaveIt() {
        assertEquals(Assigner.SELF, Assigner.of("STUDENT")); assertEquals("스스로 정한 일", Assigner.SELF.taskLabel)
        assertEquals("학부모가 준 일", Assigner.of("PARENT")!!.taskLabel); assertEquals("멘토가 만든 목표", Assigner.of("MENTOR")!!.goalLabel)
        assertNull(Assigner.of("")); assertNull(Assigner.of(null))
    }

    @Test
    fun onlyTreeGoalsAndNoArchivedChildren() {
        val goals = listOf(goal("a"), goal("b", leadsTo = "a", status = GoalStatus.ARCHIVED), Fixtures.goal("트랙 목표", trackId = "track"))
        assertEquals(listOf("a", "b"), GoalTree.nodes(goals, emptyList(), today, utc).map { it.goal.id })
        assertTrue(GoalTree.node(goals[0], goals, emptyList(), today, utc).children.isEmpty())
    }
}
