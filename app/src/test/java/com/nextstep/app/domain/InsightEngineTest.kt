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
}

class StudyPlannerTest {
    private val family = "fam"
    private val math = SubjectEntity(id = "math", familyId = family, name = "수학", color = 0xFF3B82F6)

    @Test
    fun plannerSkipsSlotsThatOverlapExistingEvents() {
        val topics = listOf(
            TopicEntity(familyId = family, subjectId = "math", title = "A", orderIndex = 0, classCovered = true, status = TopicStatus.IN_CLASS),
            TopicEntity(familyId = family, subjectId = "math", title = "B", orderIndex = 1, classCovered = true, status = TopicStatus.IN_CLASS),
            TopicEntity(familyId = family, subjectId = "math", title = "C", orderIndex = 2),
        )
        val progress = StudyStats.subjectProgress(topics, listOf(math))
        val queue = StudyPlanner.buildQueue(progress, emptyList(), listOf(math))
        assertEquals(listOf("복습: 수학 A", "복습: 수학 B", "예습: 수학 C"), queue.map { it.title })

        val from = java.time.LocalDate.of(2026, 9, 23)
        val busy = com.nextstep.app.data.local.EventEntity(
            familyId = family, title = "학원", startAt = DateUtils.toMillis(from, java.time.LocalTime.of(19, 0)), endAt = DateUtils.toMillis(from, java.time.LocalTime.of(20, 30)),
        )
        val options = PlanOptions(days = 2, startTime = java.time.LocalTime.of(19, 0), sessionMinutes = 50, breakMinutes = 10, sessionsPerDay = 2)
        val plan = StudyPlanner.generate(queue, listOf(busy), options, from)
        // 첫날 두 슬롯(19:00, 20:00)은 학원과 겹쳐 건너뛰고, 둘째 날 두 슬롯만 배치됩니다.
        assertEquals(2, plan.events.size)
        assertEquals(2, plan.tasks.size)
        assertTrue(plan.events.all { DateUtils.toLocalDate(it.startAt) == from.plusDays(1) })
        assertEquals("복습: 수학 A", plan.events.first().title)
        assertEquals("수학 A", plan.tasks.first().title)
    }

    @Test
    fun talentsFlagEfficientSubject() {
        val eng = SubjectEntity(id = "eng", familyId = family, name = "영어", color = 0xFF10B981)
        val base = DateUtils.toMillis(java.time.LocalDate.of(2026, 9, 1), java.time.LocalTime.of(20, 0))
        val sessions = listOf(
            StudySessionEntity(familyId = family, subjectId = "math", startAt = base, endAt = base + 1, durationMinutes = 300),
            StudySessionEntity(familyId = family, subjectId = "eng", startAt = base, endAt = base + 1, durationMinutes = 30),
        )
        val grades = listOf(
            GradeEntity(familyId = family, subjectId = "math", title = "t", score = 70.0, date = 1),
            GradeEntity(familyId = family, subjectId = "eng", title = "t", score = 95.0, date = 1),
        )
        val talents = InsightEngine.talents(listOf(math, eng), emptyList(), grades, sessions)
        assertTrue(talents.any { it.subjectId == "eng" && it.title.contains("효율형") })
    }
}
