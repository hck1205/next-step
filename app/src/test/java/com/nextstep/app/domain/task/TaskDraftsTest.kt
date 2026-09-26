package com.nextstep.app.domain.task

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.curriculum.CurriculumUnit
import com.nextstep.app.domain.growth.StudyKind
import com.nextstep.app.domain.growth.StudyKindType
import com.nextstep.app.domain.insight.InsightAction
import com.nextstep.app.domain.taskboard.SuggestionSource
import com.nextstep.app.domain.taskboard.TaskSuggestion
import com.nextstep.app.domain.year.YearArea
import com.nextstep.app.domain.year.YearDoer
import com.nextstep.app.domain.year.YearTask
import com.nextstep.app.domain.year.YearTerm
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TaskDraftsTest {
    private val today = LocalDate.of(2026, 9, 24)
    private val topic = Fixtures.topic("math", "분수", 0)

    @Test
    fun topicTaskIsNamedSubjectTopicKindAndStudentsDoItToday() {
        val t = TaskDrafts.forTopic(Fixtures.math, topic, TaskType.REVIEW, TaskDrafts.topicDue(Role.STUDENT.name, today), Role.STUDENT.name)
        assertEquals("수학 분수 복습", t.title)
        assertEquals("math", t.subjectId); assertEquals(topic.id, t.topicId)
        assertEquals(today.toEpochDay(), t.dueDate)
        assertEquals(today.plusDays(1), TaskDrafts.topicDue(Role.MENTOR.name, today))
        assertEquals(today.plusDays(1), TaskDrafts.topicDue(Role.PARENT.name, today))
    }

    @Test
    fun studyKindAndYearTaskBecomeTodaysOwnTasks() {
        val kind = TaskDrafts.forStudyKind(StudyKind("받아쓰기", StudyKindType.TEST_PREP, 2, 10), today)
        assertEquals("받아쓰기 10분", kind.title); assertEquals(TaskType.EXAM_PREP, kind.type); assertTrue(kind.isStudentMade)
        val year = TaskDrafts.forYearTask(YearTask(YearArea.EXAM, YearTerm.FIRST, "단원평가", "틀린 문제 다시 풀기", YearDoer.CHILD), today)
        assertEquals(TaskType.EXAM_PREP, year.type); assertEquals("틀린 문제 다시 풀기", year.note); assertEquals(today.toEpochDay(), year.dueDate)
    }

    @Test
    fun insightTaskIsDueTomorrowAndSuggestionKeepsGoalAndReason() {
        val insight = TaskDrafts.forInsight(InsightAction.CreateTask("수학 분수 복습", "math", topic.id, TaskType.REVIEW), Role.PARENT.name, today)
        assertEquals(today.plusDays(1).toEpochDay(), insight.dueDate); assertEquals(Role.PARENT.name, insight.createdByRole)
        val s = TaskSuggestion("math", topic.id, "분수 복습", TaskType.REVIEW, SuggestionSource.values().first(), today)
        val goal = Fixtures.goal("분수 끝내기", trackId = "tree")
        val withGoal = TaskDrafts.forSuggestion(s, goal, Role.STUDENT.name)
        assertEquals(goal.id, withGoal.goalId); assertEquals("분수 끝내기 · ${s.source.label}", withGoal.note)
        val alone = TaskDrafts.forSuggestion(s, null as GoalEntity?, Role.STUDENT.name)
        assertNull(alone.goalId); assertEquals(s.source.label, alone.note)
    }

    @Test
    fun writtenAndCurriculumTasksKeepWhatWasGiven() {
        val w = TaskDrafts.written("단어 30개", null, TaskType.HOMEWORK, today, Role.MENTOR.name)
        assertEquals("단어 30개", w.title); assertNull(w.subjectId); assertEquals(Role.MENTOR.name, w.createdByRole)
        val c = TaskDrafts.forCurriculum(CurriculumUnit("수학", "약수와 배수"), "math", TaskType.PREVIEW, today, Role.STUDENT.name)
        assertEquals("수학 · 약수와 배수", c.title); assertEquals("이번 학기 커리큘럼", c.note)
    }
}
