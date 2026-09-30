package com.nextstep.app.domain.content

import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.stats.UpcomingExam
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentRecommenderTest {
    private val subjects = listOf(Fixtures.math, Fixtures.english)
    private val topics = listOf(
        Fixtures.topic("math", "일차방정식", 0, covered = true, status = com.nextstep.app.data.model.TopicStatus.IN_CLASS),
        Fixtures.topic("math", "함수", 1),
    )
    private val progress = StudyStats.subjectProgress(topics, subjects)

    @Test
    fun reviewTopicMatchOutranksPreviewAndGenericSubject() {
        val contents = listOf(
            Fixtures.content("함수 개념", "수학"),
            Fixtures.content("일차방정식 문제 풀이", "수학", ContentType.PROBLEM),
            Fixtures.content("영어 관계대명사", "영어"),
        )
        val recs = ContentRecommender.recommend(contents, subjects, progress, emptyList(), emptyList())
        assertEquals(listOf("c-일차방정식 문제 풀이", "c-함수 개념", "c-영어 관계대명사"), recs.map { it.content.id })
        assertTrue(recs[0].reason.contains("복습"))
        assertTrue(recs[1].reason.contains("예습"))
    }

    @Test
    fun watchedContentIsNeverRecommended() {
        val recs = ContentRecommender.recommend(listOf(Fixtures.content("함수 개념", "수학", watched = true)), subjects, progress, emptyList(), emptyList())
        assertTrue(recs.isEmpty())
    }

    @Test
    fun examPrepForImminentExamAndConceptForWeakSubjectGetReasons() {
        val exam = UpcomingExam("중간고사", "eng", DateUtils.today().plusDays(5))
        val weak = StudyStats.subjectScores(listOf(Fixtures.grade("math", 55.0, 1)), subjects)
        val contents = listOf(Fixtures.content("영어 시험 대비", "영어", ContentType.EXAM_PREP), Fixtures.content("수학 기초 개념", "수학", ContentType.CONCEPT))
        val recs = ContentRecommender.recommend(contents, subjects, emptyList(), weak, listOf(exam))
        assertTrue(recs.first { it.content.subjectKey == "영어" }.reason.contains("시험"))
        assertTrue(recs.first { it.content.subjectKey == "수학" }.reason.contains("개념"))
    }

    @Test
    fun gradeLevelMismatchIsPenalized() {
        val forHigh = Fixtures.content("고등 수학", "수학").copy(gradeLevel = GradeLevel.HIGH)
        val forAll = Fixtures.content("전체 수학", "수학")
        val recs = ContentRecommender.recommend(listOf(forHigh, forAll), subjects, emptyList(), emptyList(), emptyList(), gradeLevel = GradeLevel.MIDDLE)
        assertEquals("c-전체 수학", recs.first().content.id)
    }

    @Test
    fun limitAndZeroScoreFiltering() {
        val many = (1..8).map { Fixtures.content("수학 $it", "수학") }
        assertEquals(3, ContentRecommender.recommend(many, subjects, emptyList(), emptyList(), emptyList(), limit = 3).size)
        val unrelated = Fixtures.content("요리", "요리").copy(gradeLevel = GradeLevel.HIGH)
        assertTrue(ContentRecommender.recommend(listOf(unrelated), subjects, emptyList(), emptyList(), emptyList(), gradeLevel = GradeLevel.ELEMENTARY).isEmpty())
    }

    @Test
    fun matchesUsesTitleTokensAndKeywords() {
        assertTrue(ContentRecommender.matches(Fixtures.content("아무거나", keywords = "이차함수, 그래프"), "3. 이차함수"))
        assertFalse(ContentRecommender.matches(Fixtures.content("아무거나"), "12"))
    }
}
