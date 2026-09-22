package com.nextstep.app.domain

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.model.TopicStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.content.ContentRecommender
import com.nextstep.app.domain.content.ContentClassifier
import com.nextstep.app.domain.content.YouTubeLinks

class ContentTest {
    @Test
    fun youTubeLinksExtractIds() {
        assertEquals("dQw4w9WgXcQ", YouTubeLinks.videoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=10s"))
        assertEquals("dQw4w9WgXcQ", YouTubeLinks.videoId("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", YouTubeLinks.videoId("https://youtube.com/shorts/dQw4w9WgXcQ"))
        assertNull(YouTubeLinks.videoId("https://example.com/watch?v=dQw4w9WgXcQ"))
    }

    @Test
    fun classifierDetectsSubjectGradeTypeAndKeywords() {
        val c = ContentClassifier.classify("[중2 수학] 일차방정식 개념 완전정복 10분 정리", "수학쌤")
        assertEquals("수학", c.subjectKey)
        assertEquals(GradeLevel.MIDDLE, c.gradeLevel)
        assertEquals(ContentType.SUMMARY, c.contentType)
        assertTrue(c.keywords.contains("방정식") || c.keywords.contains("일차방정식"))
        assertTrue(c.reasons.isNotEmpty())
    }

    @Test
    fun classifierPrefersFamilySubjectNameAndFallsBackToConcept() {
        val c = ContentClassifier.classify("고1 한국사 조선 후기 강의 1강", familySubjectNames = listOf("한국사"))
        assertEquals("한국사", c.subjectKey)
        assertEquals(GradeLevel.HIGH, c.gradeLevel)
        assertEquals(ContentType.CONCEPT, c.contentType)
        val study = ContentClassifier.classify("성적 올리는 공부법: 오답노트 쓰는 법")
        assertEquals(ContentType.STUDY_METHOD, study.contentType)
        assertEquals("", study.subjectKey)
    }

    @Test
    fun recommenderRanksReviewTopicMatchFirst() {
        val family = "fam"
        val math = SubjectEntity(id = "math", familyId = family, name = "수학", color = 0xFF3B82F6)
        val topics = listOf(
            TopicEntity(familyId = family, subjectId = "math", title = "일차방정식", orderIndex = 0, classCovered = true, status = TopicStatus.IN_CLASS),
            TopicEntity(familyId = family, subjectId = "math", title = "함수", orderIndex = 1),
        )
        val progress = StudyStats.subjectProgress(topics, listOf(math))
        val contents = listOf(
            ContentEntity(id = "a", familyId = family, url = "u1", title = "함수 개념", subjectKey = "수학", contentType = ContentType.CONCEPT),
            ContentEntity(id = "b", familyId = family, url = "u2", title = "일차방정식 문제 풀이", subjectKey = "수학", contentType = ContentType.PROBLEM),
            ContentEntity(id = "c", familyId = family, url = "u3", title = "영어 관계대명사", subjectKey = "영어", contentType = ContentType.CONCEPT),
        )
        val recs = ContentRecommender.recommend(contents, listOf(math), progress, emptyList(), emptyList())
        assertEquals("b", recs.first().content.id)
        assertTrue(recs.first().reason.contains("복습"))
        assertEquals("a", recs[1].content.id)
    }
}
