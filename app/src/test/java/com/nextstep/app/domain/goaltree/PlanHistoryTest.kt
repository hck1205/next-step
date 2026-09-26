package com.nextstep.app.domain.goaltree

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class PlanHistoryTest {
    private val utc = ZoneOffset.UTC
    private val today = LocalDate.of(2029, 5, 10) // 목요일
    private val millis = { d: LocalDate -> d.atStartOfDay(utc).toInstant().toEpochMilli() }

    private fun task(title: String, due: LocalDate, by: String = "STUDENT", subject: String? = null, done: LocalDate? = null, goalId: String? = null) = TaskEntity(
        id = title, familyId = Fixtures.FAMILY, title = title, dueDate = due.toEpochDay(), createdByRole = by, subjectId = subject,
        done = done != null, doneAt = done?.let(millis), goalId = goalId,
    )

    @Test
    fun weeklyRatesCountTasksDueThatWeek() {
        val tasks = listOf(task("a", today, done = today), task("b", today.minusDays(1)), task("c", today.minusWeeks(1), done = today.minusWeeks(1)), task("d", today.minusWeeks(9)))
        val w = PlanHistory.weeks(tasks, today)
        assertEquals(8, w.size); assertEquals(LocalDate.of(2029, 5, 7), w.last().weekStart)
        assertEquals(2, w.last().due); assertEquals(1, w.last().done); assertEquals(0.5f, w.last().rate!!, 0.001f)
        assertEquals(1f, w[6].rate!!, 0f); assertNull(w.first().rate)
    }

    @Test
    fun ratesByWhoGaveItAndBySubject() {
        val subjects = listOf(Fixtures.math)
        val tasks = listOf(
            task("a", today, "STUDENT", "math", done = today), task("b", today, "PARENT"), task("c", today.minusDays(3), "PARENT", done = today),
            task("d", today.plusDays(3), "MENTOR"), task("e", today.minusDays(40), "MENTOR"),
        )
        assertEquals(listOf("스스로" to 1f, "학부모가" to 0.5f), PlanHistory.byAssigner(tasks, today).map { it.label to it.rate })
        assertEquals(listOf("과목 밖" to 2, "수학" to 1), PlanHistory.bySubject(tasks, subjects, today).map { it.label to it.total })
        assertEquals(2f / 3, PlanHistory.recentRate(tasks, today)!!, 0.001f)
        assertNull(PlanHistory.recentRate(emptyList(), today))
    }

    @Test
    fun timelineShowsStartsDoneTasksAndAchievementsNewestFirst() {
        val big = GoalEntity(id = "big", familyId = Fixtures.FAMILY, trackId = GoalTree.TRACK, title = "영어로 말하기", createdAt = millis(today.minusDays(30)))
        val small = GoalEntity(
            id = "s", familyId = Fixtures.FAMILY, trackId = GoalTree.TRACK, title = "파닉스 끝내기", leadsTo = "big", status = GoalStatus.DONE,
            createdAt = millis(today.minusDays(20)), doneAt = millis(today.minusDays(1)), createdByRole = "PARENT",
        )
        val tasks = listOf(task("단어 20개", today.minusDays(2), done = today, goalId = "s"), task("미완료", today))
        val t = PlanHistory.timeline(listOf(big, small), tasks, utc)
        assertEquals(listOf(HistoryKind.TASK_DONE, HistoryKind.GOAL_ACHIEVED, HistoryKind.GOAL_STARTED, HistoryKind.GOAL_STARTED), t.map { it.kind })
        assertEquals("파닉스 끝내기", t[0].goalTitle); assertEquals("영어로 말하기", t[1].leadsToTitle)
    }
}
