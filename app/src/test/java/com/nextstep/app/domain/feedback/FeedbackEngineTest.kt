package com.nextstep.app.domain.feedback

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackEngineTest {
    private val today = LocalDate.of(2029, 5, 20)
    private val subjects = listOf(Fixtures.math, Fixtures.english)
    private fun study(daysAgo: Long, subject: String = "math", minutes: Int = 30) = Fixtures.session(subject, today.minusDays(daysAgo), LocalTime.of(19, 0), minutes)
    private fun doneTask(title: String, daysAgo: Long, by: String) = Fixtures.task(title, today.minusDays(daysAgo), done = true, by = by)
        .copy(doneAt = DateUtils.toMillis(today.minusDays(daysAgo), LocalTime.NOON))

    private fun find(sessions: List<StudySessionEntity> = emptyList(), tasks: List<TaskEntity> = emptyList(), grades: List<GradeEntity> = emptyList(), topics: List<TopicEntity> = emptyList()) =
        FeedbackEngine.findings(subjects, topics, grades, sessions, tasks, today)

    @Test
    fun studyDaysAreComparedWithLastWeekOnly() {
        // 이번 주 5일, 지난주 2일 → 늘었어요
        val up = find(sessions = (0L..4L).map { study(it) } + listOf(study(8), study(10)))
        assertEquals(Finding(FeedbackKind.STUDY_DAYS_UP, 5, 2), up.single { it.kind == FeedbackKind.STUDY_DAYS_UP })
        // 이번 주 1일, 지난주 4일 → 줄었어요
        val down = find(sessions = listOf(study(0)) + (7L..10L).map { study(it) })
        assertEquals(Finding(FeedbackKind.STUDY_DAYS_DOWN, 1, 4), down.single { it.kind == FeedbackKind.STUDY_DAYS_DOWN })
        // 4일 이상 비슷하면 꾸준해요, 기록이 없으면 아무 말도 하지 않아요
        assertTrue(find(sessions = (0L..3L).map { study(it) } + (7L..10L).map { study(it) }).any { it.kind == FeedbackKind.STUDY_STEADY })
        assertTrue(find().isEmpty())
    }

    @Test
    fun tasksDoneWellOverdueAndSelfMadeShare() {
        val due = listOf(Fixtures.task("a", today, done = true), Fixtures.task("b", today.minusDays(1), done = true), Fixtures.task("c", today.minusDays(2), done = true),
            Fixtures.task("d", today.minusDays(3), done = true), Fixtures.task("e", today.minusDays(4)))
        assertEquals(Finding(FeedbackKind.TASKS_WELL, 4, 5), find(tasks = due).single { it.kind == FeedbackKind.TASKS_WELL })
        val overdue = (10L..12L).map { Fixtures.task("o$it", today.minusDays(it)) }
        assertEquals(3, find(tasks = overdue).single { it.kind == FeedbackKind.TASKS_OVERDUE }.now)
        // 스스로 정한 일: 지난주 1/2 → 이번 주 3/4 (50% → 75%)
        val self = listOf(doneTask("s1", 1, "STUDENT"), doneTask("s2", 2, "STUDENT"), doneTask("s3", 3, "STUDENT"), doneTask("p1", 3, "PARENT"),
            doneTask("s4", 8, "STUDENT"), doneTask("p2", 9, "PARENT"))
        assertEquals(Finding(FeedbackKind.SELF_MADE_UP, 75, 50), find(tasks = self).single { it.kind == FeedbackKind.SELF_MADE_UP })
    }

    @Test
    fun subjectFindingsCarryTheSubject() {
        val grades = listOf(Fixtures.grade("math", 70.0, today.minusDays(30).toEpochDay()), Fixtures.grade("math", 85.0, today.minusDays(3).toEpochDay()),
            Fixtures.grade("eng", 90.0, today.minusDays(40).toEpochDay()), Fixtures.grade("eng", 80.0, today.minusDays(35).toEpochDay()))
        val scores = find(grades = grades).filter { it.kind == FeedbackKind.SCORE_UP || it.kind == FeedbackKind.SCORE_DOWN }
        assertEquals(listOf(Finding(FeedbackKind.SCORE_UP, 85, 70, "math", "수학")), scores) // 영어는 최근 점수가 2주보다 오래돼 말하지 않음
        // 영어를 그 전 3주에 했는데 이번 주엔 안 함
        val gap = find(sessions = listOf(study(0, "math"), study(9, "eng", 40), study(12, "eng", 40))).single { it.kind == FeedbackKind.SUBJECT_GAP }
        assertEquals("eng", gap.subjectId); assertEquals(9, gap.now)
        val topics = (1..4).map { Fixtures.topic("math", "단원$it", it, covered = true, status = if (it == 4) TopicStatus.REVIEWED else TopicStatus.NOT_STARTED) }
        assertEquals(Finding(FeedbackKind.REVIEW_BACKLOG, 3, subjectId = "math", subjectName = "수학"), find(topics = topics).single())
    }

    @Test
    fun eachAudienceGetsOneGoodThingFirstAndMentorsOnlyTheirSubjects() {
        val all = listOf(
            Finding(FeedbackKind.TASKS_OVERDUE, 3), Finding(FeedbackKind.SELF_MADE_UP, 75, 50), Finding(FeedbackKind.STUDY_DAYS_UP, 5, 2),
            Finding(FeedbackKind.SCORE_DOWN, 70, 85, "eng", "영어"), Finding(FeedbackKind.REVIEW_BACKLOG, 3, subjectId = "math", subjectName = "수학"),
        )
        val parent = FeedbackEngine.forAudience(all, FeedbackAudience.PARENT)
        assertEquals(listOf(FeedbackKind.SELF_MADE_UP, FeedbackKind.TASKS_OVERDUE, FeedbackKind.SCORE_DOWN), parent.map { it.kind })
        // 멘토(수학 담당): 스스로 정한 몫(가족의 일)과 영어는 빠짐
        val mentor = FeedbackEngine.forAudience(all, FeedbackAudience.MENTOR, mentorSubjects = setOf("math"))
        assertEquals(listOf(FeedbackKind.STUDY_DAYS_UP, FeedbackKind.TASKS_OVERDUE, FeedbackKind.REVIEW_BACKLOG), mentor.map { it.kind })
        assertTrue(FeedbackEngine.forAudience(all, FeedbackAudience.STUDENT).size <= FeedbackEngine.MAX_LINES)
    }
}
