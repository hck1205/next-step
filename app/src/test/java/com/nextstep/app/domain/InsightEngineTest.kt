package com.nextstep.app.domain

import com.nextstep.app.data.local.GradeEntity
import com.nextstep.app.data.local.StudySessionEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
            TopicEntity(familyId = family, subjectId = "math", title = "단원 $i", orderIndex = i, classCovered = i < 4, status = if (i == 0) TopicStatus.REVIEWED else TopicStatus.IN_CLASS)
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
}
