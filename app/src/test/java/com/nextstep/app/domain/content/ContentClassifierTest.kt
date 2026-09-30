package com.nextstep.app.domain.content

import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentClassifierTest {
    @Test
    fun detectsSubjectGradeTypeAndKeywords() {
        val c = ContentClassifier.classify("[중2 수학] 일차방정식 개념 완전정복 10분 정리", "수학쌤")
        assertEquals("수학", c.subjectKey)
        assertEquals(GradeLevel.MIDDLE, c.gradeLevel)
        assertEquals(ContentType.SUMMARY, c.contentType)
        assertTrue(c.keywords.any { it.contains("방정식") })
        assertTrue(c.reasons.size >= 3)
    }

    @Test
    fun familySubjectNameWinsOverDictionary() {
        val c = ContentClassifier.classify("고1 한국사 조선 후기 강의 1강", familySubjectNames = listOf("한국사"))
        assertEquals("한국사", c.subjectKey)
        assertEquals(GradeLevel.HIGH, c.gradeLevel)
        assertEquals(ContentType.CONCEPT, c.contentType)
    }

    @Test
    fun typePriorityPrefersExamPrepOverConcept() {
        assertEquals(ContentType.EXAM_PREP, ContentClassifier.classify("중간고사 대비 수학 개념 강의").contentType)
        assertEquals(ContentType.PROBLEM, ContentClassifier.classify("영어 유형별 문제 풀이 해설").contentType)
        assertEquals(ContentType.STUDY_METHOD, ContentClassifier.classify("성적 올리는 공부법: 오답노트 쓰는 법").contentType)
        assertEquals(ContentType.MOTIVATION, ContentClassifier.classify("슬럼프 극복 이야기").contentType)
    }

    @Test
    fun unknownTitleFallsBackToOtherWithoutSubject() {
        val c = ContentClassifier.classify("재미있는 브이로그")
        assertEquals("", c.subjectKey)
        assertEquals(ContentType.OTHER, c.contentType)
        assertEquals(GradeLevel.ALL, c.gradeLevel)
        assertTrue(c.reasons.isEmpty())
    }

    @Test
    fun singleCharacterHintsDoNotMatch() {
        assertEquals("", ContentClassifier.classify("성적 올리는 공부법").subjectKey) // "법" 이 사회로 잡히면 안 됨
        assertEquals("과학", ContentClassifier.classify("빛과 파동 기초").subjectKey)
    }

    @Test
    fun keywordsSkipStopWordsAndNumbers() {
        val c = ContentClassifier.classify("2026 수학 강의 영상 part 3 삼각함수")
        assertFalse(c.keywords.any { it == "강의" || it == "영상" || it == "part" || it.all { ch -> ch.isDigit() } })
        assertTrue(c.keywords.contains("삼각함수"))
        assertTrue(c.keywords.size <= 8)
    }

    @Test
    fun subjectIsPickedByHighestHintCount() {
        assertEquals("영어", ContentClassifier.classify("관계대명사 분사 가정법 총정리 수학 아님").subjectKey)
    }
}
