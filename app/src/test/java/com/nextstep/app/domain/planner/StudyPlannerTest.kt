package com.nextstep.app.domain.planner

import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class StudyPlannerTest {
    private val subjects = listOf(Fixtures.math, Fixtures.english)
    private val from = LocalDate.of(2026, 9, 23) // 수요일

    private fun progress(mathCovered: Int = 2, engCovered: Int = 1) = StudyStats.subjectProgress(
        Fixtures.topics("math", 4, covered = mathCovered) + Fixtures.topics("eng", 3, covered = engCovered), subjects,
    )

    @Test
    fun queueInterleavesReviewThenRoadmapThenPreview() {
        val roadmap = listOf(
            Fixtures.roadmap("나중", status = RoadmapStatus.PLANNED, target = from.plusDays(9)),
            Fixtures.roadmap("진행", status = RoadmapStatus.IN_PROGRESS),
            Fixtures.roadmap("끝", status = RoadmapStatus.DONE),
        )
        val queue = StudyPlanner.buildQueue(progress(), roadmap, subjects)
        assertEquals(
            listOf("복습: 수학 단원 0", "복습: 영어 단원 0", "복습: 수학 단원 1", "로드맵: 진행", "로드맵: 나중", "예습: 수학 단원 2", "예습: 영어 단원 1"),
            queue.map { it.title },
        )
        assertEquals(TaskType.REVIEW, queue.first().taskType)
        assertEquals("r-진행", queue[3].roadmapId)
    }

    @Test
    fun generateFillsSlotsInOrderAndCreatesTasksWithTopicLinks() {
        val queue = StudyPlanner.buildQueue(progress(), emptyList(), subjects)
        val plan = StudyPlanner.generate(queue, emptyList(), PlanOptions(days = 2, sessionsPerDay = 2, startTime = LocalTime.of(19, 0), sessionMinutes = 50, breakMinutes = 10), from)
        assertEquals(4, plan.events.size)
        assertEquals(LocalTime.of(20, 0), DateUtils.toLocalDateTime(plan.events[1].startAt).toLocalTime())
        assertEquals(from.plusDays(1), DateUtils.toLocalDate(plan.events[2].startAt))
        assertEquals("t-math-0", plan.tasks.first().topicId)
        assertEquals(from.toEpochDay(), plan.tasks.first().dueDate)
        assertTrue(plan.events.all { it.memo.isNotBlank() })
    }

    @Test
    fun generateSkipsWeekendWhenExcluded() {
        val queue = StudyPlanner.buildQueue(progress(), emptyList(), subjects)
        val saturday = LocalDate.of(2026, 9, 26)
        val plan = StudyPlanner.generate(queue, emptyList(), PlanOptions(days = 3, sessionsPerDay = 1, includeWeekend = false), saturday)
        assertEquals(1, plan.events.size) // 토·일 건너뛰고 월요일만
        assertEquals(DayOfWeek.MONDAY, DateUtils.toLocalDate(plan.events.single().startAt).dayOfWeek)
    }

    @Test
    fun generateStopsAtMidnightAndWithEmptyQueue() {
        assertTrue(StudyPlanner.generate(emptyList(), emptyList(), PlanOptions(), from).isEmpty)
        val queue = StudyPlanner.buildQueue(progress(), emptyList(), subjects)
        val late = StudyPlanner.generate(queue, emptyList(), PlanOptions(days = 1, sessionsPerDay = 5, startTime = LocalTime.of(23, 0), sessionMinutes = 50, breakMinutes = 10), from)
        assertEquals(1, late.events.size) // 23:00 하나, 다음 슬롯은 자정을 넘겨 중단
    }

    @Test
    fun roadmapTasksCarryRoadmapReferenceInNote() {
        val queue = StudyPlanner.buildQueue(progress(0, 0), listOf(Fixtures.roadmap("개념")), subjects)
        val plan = StudyPlanner.generate(queue, emptyList(), PlanOptions(days = 1, sessionsPerDay = 1), from)
        assertEquals("roadmap:r-개념", plan.tasks.single().note)
        assertEquals("개념", plan.tasks.single().title)
    }
}
