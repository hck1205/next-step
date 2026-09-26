package com.nextstep.app.domain.insight

import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class InsightEngineTest {
    private val subjects = listOf(Fixtures.math, Fixtures.english)
    private val today = DateUtils.today()

    private fun analyze(
        topics: List<TopicEntity> = emptyList(),
        grades: List<GradeEntity> = emptyList(),
        sessions: List<StudySessionEntity> = emptyList(),
        tasks: List<TaskEntity> = emptyList(),
        events: List<EventEntity> = emptyList(),
    ) = InsightEngine.analyze(subjects, topics, grades, sessions, tasks, events)

    @Test
    fun noDataGivesOneHint() {
        val out = InsightEngine.analyze(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(1, out.size)
        assertEquals("데이터를 쌓아 보세요", out.single().title)
    }

    @Test
    fun bestSubjectOver80IsAStrengthAndWeakOnesGetAReviewTask() {
        val grades = listOf(Fixtures.grade("math", 92.0, 1), Fixtures.grade("eng", 60.0, 1))
        val out = analyze(grades = grades)
        assertTrue(out.any { it.kind == InsightKind.STRENGTH && it.subjectId == "math" })
        val weak = out.first { it.kind == InsightKind.WEAKNESS && it.subjectId == "eng" }
        assertEquals(TaskType.REVIEW, (weak.action as InsightAction.CreateTask).type)
    }

    @Test
    fun bigDropIsAnAlertAndAlertsComeFirst() {
        val grades = listOf(Fixtures.grade("math", 90.0, 1), Fixtures.grade("math", 75.0, 2))
        val out = analyze(grades = grades)
        assertTrue(out.any { it.kind == InsightKind.ALERT && it.title.contains("떨어졌어요") })
        assertEquals(InsightKind.ALERT, out.first().kind)
    }

    @Test
    fun reviewBacklogOfThreeUnitsSuggestsTheFirstOne() {
        val out = analyze(topics = Fixtures.topics("math", 6, covered = 4, reviewed = 1))
        val alert = out.first { it.title.contains("복습이 3개 단원") }
        assertEquals("수학 단원 1 복습", (alert.action as InsightAction.CreateTask).title)
        assertFalse(out.any { it.subjectId == "math" && it.title.contains("예습") })
    }

    @Test
    fun timeCrowdedIntoOneSubjectIsFlagged() {
        val sessions = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 200), Fixtures.session("eng", today, LocalTime.of(12, 0), 20))
        val out = analyze(sessions = sessions)
        assertTrue(out.any { it.title == "수학에 시간이 몰려 있어요" })
        assertTrue(out.any { it.title == "영어 학습 시간이 부족해요" })
        assertTrue(out.any { it.title.startsWith("9시~10시") })
    }

    @Test
    fun examWithoutPrepAsksForAPrepTaskButNotWhenOneExists() {
        val exam = Fixtures.event("중간고사", today.plusDays(5), LocalTime.of(9, 0), LocalTime.of(10, 0), type = EventType.EXAM, subjectId = "math")
        val without = analyze(events = listOf(exam))
        assertEquals(TaskType.EXAM_PREP, (without.first { it.title.startsWith("중간고사") }.action as InsightAction.CreateTask).type)
        val prep = Fixtures.task("준비", today.plusDays(1), subjectId = "math", type = TaskType.EXAM_PREP)
        assertFalse(analyze(tasks = listOf(prep), events = listOf(exam)).any { it.title.startsWith("중간고사") })
    }

    @Test
    fun overdueTasksAreCounted() {
        val tasks = listOf(Fixtures.task("숙제", today.minusDays(2)), Fixtures.task("단어", today.minusDays(1)))
        assertTrue(analyze(tasks = tasks).any { it.title == "기한이 지난 할 일이 2개 있어요" })
    }
}
