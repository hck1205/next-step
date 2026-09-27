package com.nextstep.app.domain.task

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.curriculum.CurriculumUnit
import com.nextstep.app.domain.growth.StudyKind
import com.nextstep.app.domain.growth.StudyKindType
import com.nextstep.app.domain.insight.InsightAction
import com.nextstep.app.domain.journey.PeriodCalendar
import com.nextstep.app.domain.mission.MissionKind
import com.nextstep.app.domain.taskboard.SuggestionSource
import com.nextstep.app.domain.taskboard.TaskSuggestion
import com.nextstep.app.domain.year.YearArea
import com.nextstep.app.domain.year.YearDoer
import com.nextstep.app.domain.year.YearTask
import com.nextstep.app.domain.year.YearTerm
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
        val year = TaskDrafts.forYearTask(YearTask(YearArea.EXAM, YearTerm.FIRST, "단원평가", "틀린 문제 다시 풀기", YearDoer.CHILD), today, Role.STUDENT.name)
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

    @Test
    fun editKeepsWhoGaveItAndWhetherItIsDone() {
        val old = TaskDrafts.written("익힘책", "math", TaskType.HOMEWORK, today, Role.PARENT.name).copy(done = true)
        val t = TaskDrafts.edited(old, "익힘책 2쪽", null, TaskType.REVIEW, today.plusDays(1))
        assertEquals(old.id, t.id); assertEquals(Role.PARENT.name, t.createdByRole); assertTrue(t.done)
        assertEquals("익힘책 2쪽", t.title); assertNull(t.subjectId); assertEquals(today.plusDays(1).toEpochDay(), t.dueDate)
    }

    @Test
    fun goalStepTaskUsesStepDateThenPeriodEndThenAWeek() {
        val step = Fixtures.step("g", "g3s1", "나눗셈 개념").copy(detail = "곱셈과의 관계")
        val period = PeriodCalendar.periods(LocalDate.of(2017, 5, 15)).first { it.key == "g3s1" }
        val inTerm = period.start.plusDays(10)
        val t = TaskDrafts.forGoalStep(step, Fixtures.goal("초등 수학"), period, inTerm, Role.PARENT.name)
        assertEquals(period.end.toEpochDay(), t.dueDate); assertEquals(TaskType.OTHER, t.type)
        assertEquals("초등 수학 · 곱셈과의 관계", t.note); assertEquals(Role.PARENT.name, t.createdByRole); assertEquals("", t.familyId)
        val later = period.end.plusDays(30)
        assertEquals(later.plusDays(7).toEpochDay(), TaskDrafts.forGoalStep(step, null, period, later, "STUDENT").dueDate)
        assertEquals("곱셈과의 관계", TaskDrafts.forGoalStep(step, null, null, later, "STUDENT").note)
        val dated = step.copy(dueDate = today.plusDays(4).toEpochDay())
        assertEquals(today.plusDays(4).toEpochDay(), TaskDrafts.forGoalStep(dated, null, null, today, "STUDENT").dueDate)
        assertEquals(today.plusDays(6).toEpochDay(), TaskDrafts.forGoalStep(dated, null, null, today.plusDays(6), "STUDENT").dueDate) // 단계 날짜가 지났으면 오늘
    }

    @Test
    fun examMissionStepsBecomeExamPrepWhereverTheyAreSent() {
        val exam = Fixtures.goal("중간고사", trackId = MissionKind.EXAM.trackId)
        val club = Fixtures.goal("동아리", trackId = MissionKind.CLUB.trackId)
        val step = Fixtures.step("g", "g8s1", "오답 노트")
        assertEquals(TaskType.EXAM_PREP, TaskDrafts.forGoalStep(step, exam, null, today, "STUDENT").type)
        assertEquals(TaskType.OTHER, TaskDrafts.forGoalStep(step, club, null, today, "STUDENT").type)
    }
}
