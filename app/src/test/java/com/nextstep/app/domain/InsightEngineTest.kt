package com.nextstep.app.domain

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.planner.StudyPlanner
import com.nextstep.app.domain.planner.PlanOptions
import com.nextstep.app.domain.insight.InsightKind
import com.nextstep.app.domain.insight.InsightAction
import com.nextstep.app.domain.insight.InsightEngine
import com.nextstep.app.domain.time.DateUtils

class InsightEngineTest {
    private val family = "fam"
    private val math = SubjectEntity(id = "math", familyId = family, name = "수학", color = 0xFF3B82F6, weeklyGoalMinutes = 180)
    private val english = SubjectEntity(id = "eng", familyId = family, name = "영어", color = 0xFF10B981, weeklyGoalMinutes = 180)

    private fun grade(subject: String, score: Double, day: Long, classAvg: Double? = null) =
        GradeEntity(familyId = family, subjectId = subject, title = "t", score = score, date = day, classAverage = classAvg)

    @Test
    fun weakSubjectProducesWeaknessInsightWithReviewAction() {
        val grades = listOf(grade("math", 55.0, 10), grade("eng", 92.0, 10))
        val insights = InsightEngine.analyze(listOf(math, english), emptyList(), grades, emptyList(), emptyList(), emptyList())
        val weak = insights.first { it.kind == InsightKind.WEAKNESS }
        assertEquals("math", weak.subjectId)
        assertTrue(weak.action is InsightAction.CreateTask)
        assertTrue(insights.any { it.kind == InsightKind.STRENGTH && it.subjectId == "eng" })
    }

    @Test
    fun scoreDropOfTenPointsRaisesAlert() {
        val grades = listOf(grade("math", 90.0, 1), grade("math", 75.0, 2))
        val insights = InsightEngine.analyze(listOf(math), emptyList(), grades, emptyList(), emptyList(), emptyList())
        assertTrue(insights.any { it.kind == InsightKind.ALERT && it.title.contains("떨어졌어요") })
    }

    @Test
    fun reviewBacklogIsComputedFromClassProgress() {
        val topics = (0 until 5).map { i ->
            TopicEntity(familyId = family, subjectId = "math", title = "단원 $i", orderIndex = i, classCovered = i < 4, status = when { i == 0 -> TopicStatus.REVIEWED; i < 4 -> TopicStatus.IN_CLASS; else -> TopicStatus.NOT_STARTED })
        }
        val progress = StudyStats.subjectProgress(topics, listOf(math)).single()
        assertEquals(4, progress.classCovered)
        assertEquals(1, progress.reviewed)
        assertEquals(3, progress.reviewQueue.size)
        assertEquals("단원 4", progress.previewQueue.single().title)

        val insights = InsightEngine.analyze(listOf(math), topics, emptyList(), emptyList(), emptyList(), emptyList())
        assertTrue(insights.any { it.kind == InsightKind.ALERT && it.title.contains("복습이 3개 단원 밀렸어요") })
    }

    @Test
    fun emptyDataGivesOnboardingHint() {
        val insights = InsightEngine.analyze(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(1, insights.size)
        assertEquals(InsightKind.SUGGESTION, insights.single().kind)
    }

    @Test
    fun minutesByHourAggregatesSessions() {
        val start = DateUtils.toMillis(java.time.LocalDate.of(2026, 9, 1), java.time.LocalTime.of(20, 0))
        val sessions = listOf(
            StudySessionEntity(familyId = family, subjectId = "math", startAt = start, endAt = start + 60 * 60_000L, durationMinutes = 60),
            StudySessionEntity(familyId = family, subjectId = "math", startAt = start + 86_400_000L, endAt = start + 86_400_000L + 30 * 60_000L, durationMinutes = 30),
        )
        val byHour = StudyStats.minutesByHour(sessions)
        assertEquals(90, byHour[20])
        assertEquals(90, byHour.sum())
    }

    @Test
    fun timeImbalanceAndGoalShortfallProduceSuggestions() {
        val start = DateUtils.weekStart()
        val sessions = listOf(
            StudySessionEntity(familyId = family, subjectId = "math", startAt = DateUtils.toMillis(start, java.time.LocalTime.of(20, 0)), endAt = 0, durationMinutes = 200),
            StudySessionEntity(familyId = family, subjectId = "eng", startAt = DateUtils.toMillis(start, java.time.LocalTime.of(21, 0)), endAt = 0, durationMinutes = 10),
        )
        val insights = InsightEngine.analyze(listOf(math, english), emptyList(), emptyList(), sessions, emptyList(), emptyList())
        assertTrue(insights.any { it.subjectId == "eng" && it.title.contains("학습 시간이 부족") })
        assertTrue(insights.any { it.subjectId == "math" && it.title.contains("몰려") })
    }

    @Test
    fun upcomingExamWithoutPrepTaskRaisesAlertWithAction() {
        val examDay = DateUtils.today().plusDays(5)
        val exam = com.nextstep.app.data.local.entity.EventEntity(
            familyId = family, subjectId = "math", title = "중간고사", type = com.nextstep.app.data.model.EventType.EXAM,
            startAt = DateUtils.toMillis(examDay, java.time.LocalTime.of(9, 0)), endAt = DateUtils.toMillis(examDay, java.time.LocalTime.of(10, 0)),
        )
        val without = InsightEngine.analyze(listOf(math), emptyList(), emptyList(), emptyList(), emptyList(), listOf(exam))
        val alert = without.first { it.kind == InsightKind.ALERT && it.title.startsWith("중간고사") }
        assertTrue(alert.action is InsightAction.CreateTask)
        assertEquals(com.nextstep.app.data.model.TaskType.EXAM_PREP, (alert.action as InsightAction.CreateTask).type)

        val prep = com.nextstep.app.data.local.entity.TaskEntity(familyId = family, subjectId = "math", title = "준비", type = com.nextstep.app.data.model.TaskType.EXAM_PREP, dueDate = examDay.toEpochDay(), createdByRole = "STUDENT")
        val with = InsightEngine.analyze(listOf(math), emptyList(), emptyList(), emptyList(), listOf(prep), listOf(exam))
        assertTrue(with.none { it.title.startsWith("중간고사") })
    }

    @Test
    fun overdueTasksAndClassGapAreReported() {
        val overdue = com.nextstep.app.data.local.entity.TaskEntity(familyId = family, title = "밀린 숙제", dueDate = DateUtils.today().minusDays(2).toEpochDay(), createdByRole = "STUDENT")
        val gap = listOf(grade("math", 60.0, 1, classAvg = 80.0))
        val insights = InsightEngine.analyze(listOf(math), emptyList(), gap, emptyList(), listOf(overdue), emptyList())
        assertTrue(insights.any { it.title.contains("기한이 지난 할 일이 1개") })
        assertTrue(insights.any { it.kind == InsightKind.WEAKNESS && it.title.contains("반 평균보다 20점") })
    }

    @Test
    fun alertsComeBeforeStrengths() {
        val grades = listOf(grade("math", 90.0, 1), grade("math", 75.0, 2), grade("eng", 95.0, 3))
        val insights = InsightEngine.analyze(listOf(math, english), emptyList(), grades, emptyList(), emptyList(), emptyList())
        val order = listOf(InsightKind.ALERT, InsightKind.WEAKNESS, InsightKind.SUGGESTION, InsightKind.STRENGTH)
        assertTrue(insights.zipWithNext().all { (a, b) -> order.indexOf(a.kind) <= order.indexOf(b.kind) })
    }
}
