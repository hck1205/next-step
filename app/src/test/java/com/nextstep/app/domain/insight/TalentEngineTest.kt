package com.nextstep.app.domain.insight

import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class TalentEngineTest {
    private val subjects = listOf(Fixtures.math, Fixtures.english)
    private val today = DateUtils.today()

    private fun talents(topics: List<com.nextstep.app.data.local.entity.TopicEntity> = emptyList(), grades: List<com.nextstep.app.data.local.entity.GradeEntity> = emptyList(), sessions: List<com.nextstep.app.data.local.entity.StudySessionEntity> = emptyList()) =
        TalentEngine.talents(subjects, topics, grades, sessions)

    @Test
    fun noDataYieldsNoTalents() {
        assertTrue(talents().isEmpty())
    }

    @Test
    fun efficientSubjectNeedsLowTimeShareAndHighAverage() {
        val sessions = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 300), Fixtures.session("eng", today, LocalTime.of(12, 0), 30))
        val grades = listOf(Fixtures.grade("math", 70.0, 1), Fixtures.grade("eng", 95.0, 1))
        val result = talents(grades = grades, sessions = sessions)
        assertTrue(result.any { it.subjectId == "eng" && it.title.contains("효율형") })
        assertFalse(result.any { it.subjectId == "math" && it.title.contains("효율형") })
    }

    @Test
    fun growthAndStabilityComeFromThreeGrades() {
        val rising = listOf(Fixtures.grade("math", 70.0, 1), Fixtures.grade("math", 80.0, 2), Fixtures.grade("math", 90.0, 3))
        assertTrue(talents(grades = rising).any { it.title.contains("성장세") })
        val stable = listOf(Fixtures.grade("eng", 88.0, 1), Fixtures.grade("eng", 90.0, 2), Fixtures.grade("eng", 89.0, 3))
        assertTrue(talents(grades = stable).any { it.title.contains("안정적인 실력") })
        val volatile = listOf(Fixtures.grade("eng", 60.0, 1), Fixtures.grade("eng", 95.0, 2), Fixtures.grade("eng", 70.0, 3))
        assertFalse(talents(grades = volatile).any { it.title.contains("안정적인 실력") })
    }

    @Test
    fun consistencyAndFocusComeFromSessions() {
        val nineDays = (0 until 9).map { Fixtures.session("math", today.minusDays(it.toLong()), LocalTime.of(20, 0), 20) }
        assertTrue(talents(sessions = nineDays).any { it.title == "꾸준함" })
        val long = listOf(Fixtures.session("math", today, LocalTime.of(20, 0), 95))
        assertTrue(talents(sessions = long).any { it.title == "몰입력" })
    }

    @Test
    fun selfDirectedNeedsHalfOfCoveredTopicsPreviewed() {
        val topics = (0 until 4).map { Fixtures.topic("math", "t$it", it, covered = true, status = if (it < 2) TopicStatus.PREVIEWED else TopicStatus.IN_CLASS) }
        assertTrue(talents(topics = topics).any { it.title == "자기주도 학습" })
        val few = topics.map { it.copy(status = TopicStatus.IN_CLASS) }
        assertFalse(talents(topics = few).any { it.title == "자기주도 학습" })
    }

    @Test
    fun morningLearnerDetectedFromHourDistribution() {
        val morning = (0 until 4).map { Fixtures.session("math", today.minusDays(it.toLong()), LocalTime.of(7, 0), 60) }
        assertTrue(talents(sessions = morning).any { it.title == "아침형 학습자" })
    }

    @Test
    fun highConfidenceSubjectIsReported() {
        val topics = (0 until 3).map { Fixtures.topic("math", "t$it", it, confidence = 90) }
        assertTrue(talents(topics = topics).any { it.subjectId == "math" && it.title.contains("자신감") })
    }

    @Test
    fun resultIsSortedByStrengthDescending() {
        val sessions = (0 until 9).map { Fixtures.session("math", today.minusDays(it.toLong()), LocalTime.of(20, 0), 100) }
        val result = talents(sessions = sessions)
        assertTrue(result.zipWithNext().all { (a, b) -> a.strength >= b.strength })
    }
}
