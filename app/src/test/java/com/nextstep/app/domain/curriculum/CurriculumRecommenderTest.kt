package com.nextstep.app.domain.curriculum

import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumRecommenderTest {
    private val term = CurriculumCatalog.forPeriod("g7s1")!!
    private val math = Fixtures.subject("m", "수학")

    @Test
    fun subjectAndUnitMatchingIsTolerantToNaming() {
        assertTrue(CurriculumRecommender.sameSubject("수학", "중1 수학")); assertTrue(CurriculumRecommender.sameSubject("통합과학1", "과학"))
        assertTrue(!CurriculumRecommender.sameSubject("국어", "영어")); assertTrue(!CurriculumRecommender.sameSubject("", "수학"))
        val unit = term.unitsOf("수학").first { it.title == "정수와 유리수" }
        assertTrue(CurriculumRecommender.matches(unit, "1-2. 정수와 유리수의 계산")); assertTrue(CurriculumRecommender.matches(unit, "음수 계산"))
        assertTrue(!CurriculumRecommender.matches(unit, "일차방정식"))
    }

    @Test
    fun statusFollowsFamilyTopicsAndSuggestionsFollowStatus() {
        val topics = listOf(
            Fixtures.topic("m", "정수와 유리수", 0, covered = true, status = TopicStatus.NOT_STARTED, id = "t1"),
            Fixtures.topic("m", "문자와 식", 1, status = TopicStatus.REVIEWED, id = "t2"),
            Fixtures.topic("m", "일차방정식", 2, id = "t3"),
        )
        val plan = CurriculumRecommender.plan(term, listOf(math), topics, emptyList(), emptyList(), GradeLevel.MIDDLE, "중1 1학기")
        val mathPlan = plan.subjects.first { it.subject == "수학" }
        assertEquals(math, mathPlan.familySubject)
        val byTitle = mathPlan.units.associateBy { it.unit.title }
        assertEquals(UnitStatus.IN_CLASS, byTitle.getValue("정수와 유리수").status); assertTrue(byTitle.getValue("정수와 유리수").suggestion!!.contains("복습"))
        assertEquals(UnitStatus.DONE, byTitle.getValue("문자와 식").status); assertNull(byTitle.getValue("문자와 식").suggestion)
        assertEquals(UnitStatus.REGISTERED, byTitle.getValue("일차방정식").status); assertTrue(byTitle.getValue("일차방정식").suggestion!!.contains("예습"))
        assertEquals(UnitStatus.NOT_REGISTERED, byTitle.getValue("소인수분해").status); assertTrue(byTitle.getValue("소인수분해").suggestion!!.contains("뼈대"))
        assertTrue(plan.essentialTodo.any { it.unit.title == "소인수분해" })
        assertTrue(mathPlan.notRegistered.map { it.title }.containsAll(listOf("소인수분해", "좌표평면과 그래프")))
        assertNull(plan.subjects.first { it.subject == "영어" }.familySubject)
        assertTrue(plan.registeredRatio > 0f && plan.registeredRatio < 1f)
    }

    @Test
    fun videosMatchGradeAndUnitAndSearchUrlIsBuilt() {
        val contents = listOf(
            Fixtures.content("중1 정수와 유리수 개념 강의", "수학", ContentType.CONCEPT, keywords = "정수,유리수", id = "good").copy(gradeLevel = GradeLevel.MIDDLE, ratingSum = 10, ratingCount = 2),
            Fixtures.content("고등 정수론 특강", "수학", ContentType.CONCEPT, id = "high").copy(gradeLevel = GradeLevel.HIGH),
            Fixtures.content("음수 계산 쉽게", "수학", ContentType.CONCEPT, id = "ok").copy(gradeLevel = GradeLevel.ALL, ratingSum = 3, ratingCount = 1),
            Fixtures.content("정수 세 번째", "수학", ContentType.CONCEPT, id = "third").copy(gradeLevel = GradeLevel.ALL),
            Fixtures.content("정수 지워짐", "수학", ContentType.CONCEPT, id = "gone").copy(deleted = true),
        )
        val plan = CurriculumRecommender.plan(term, emptyList(), emptyList(), emptyList(), contents, GradeLevel.MIDDLE, "중1 1학기")
        val unit = plan.subjects.first { it.subject == "수학" }.units.first { it.unit.title == "정수와 유리수" }
        assertEquals(listOf("good", "ok"), unit.videos.map { it.id })
        assertTrue(unit.searchUrl.startsWith("https://www.youtube.com/results?search_query=")); assertTrue(unit.searchUrl.contains("%EC%A0%95%EC%88%98"))
        val noVideo = plan.subjects.first { it.subject == "수학" }.units.first { it.unit.title == "일차방정식" }
        assertTrue(noVideo.videos.isEmpty())
    }

    @Test
    fun peerCountsAttachToUnitsAndExtrasAreListed() {
        val peers = listOf(
            Fixtures.peerTopic("g7s1", "수학", "정수와 유리수", 12), Fixtures.peerTopic("g7s1", "수학", "정수와 유리수 심화", 3, id = "dup"),
            Fixtures.peerTopic("g7s1", "수학", "경우의 수 맛보기", 4), Fixtures.peerTopic("g7s2", "수학", "기본 도형", 9), Fixtures.peerTopic("g7s1", "코딩", "파이썬 기초", 7),
        )
        val plan = CurriculumRecommender.plan(term, emptyList(), emptyList(), peers, emptyList(), GradeLevel.MIDDLE, "중1 1학기")
        val unit = plan.subjects.first { it.subject == "수학" }.units.first { it.unit.title == "정수와 유리수" }
        assertEquals(12, unit.peerFamilies)
        assertEquals(listOf("파이썬 기초", "경우의 수 맛보기"), plan.peerExtras.map { it.title })
    }
}
