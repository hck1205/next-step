package com.nextstep.app.domain.year

import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.growth.YearProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YearPlansTest {
    @Test
    fun everyYearProfileHasAPlanWithUniqueKeys() {
        YearProfiles.all.forEach { profile ->
            val tasks = YearPlans.forYear(profile.key)
            assertTrue("${profile.key} 할 일 없음", tasks.size >= 4)
            assertEquals("${profile.key} 키 중복", tasks.size, tasks.map { it.key }.toSet().size)
            assertTrue("${profile.key} 빈 방법", tasks.all { it.title.isNotBlank() && it.how.isNotBlank() })
        }
    }

    @Test
    fun areasFollowEnumOrderAndUnknownKeyIsEmpty() {
        YearProfiles.all.forEach { profile ->
            val areas = YearPlans.areasOf(profile.key)
            assertEquals(areas.sortedBy { it.ordinal }, areas)
            assertEquals(areas.distinct(), areas)
        }
        assertTrue(YearPlans.forYear("zz").isEmpty())
    }

    @Test
    fun preschoolYearsPlayAndTalkWithoutExamsOrRecords() {
        val school = setOf(YearArea.EXAM, YearArea.RECORD, YearArea.CAREER, YearArea.KOREAN)
        YearProfiles.all.filter { it.level == StudentUiLevel.SEED }.forEach { profile ->
            val areas = YearPlans.areasOf(profile.key)
            assertTrue(profile.key, YearArea.TALK in areas && YearArea.PLAY in areas)
            assertFalse(profile.key, areas.any { it in school })
        }
        // 영아기(만 0~2세)는 말·놀이·생활·몸·마음과 부모의 지원·서류·상담만.
        val infant = setOf(YearArea.TALK, YearArea.PLAY, YearArea.LIFE, YearArea.BODY, YearArea.MIND, YearArea.GUIDE, YearArea.ADMIN)
        listOf("a0", "a1", "a2").forEach { key -> assertTrue(key, infant.containsAll(YearPlans.areasOf(key))) }
    }

    @Test
    fun highSchoolSeniorHasExamAndCareerTabs() {
        val areas = YearPlans.areasOf("h3")
        assertTrue(YearArea.EXAM in areas)
        assertTrue(YearArea.CAREER in areas)
    }

    @Test
    fun storageIdIsPrefixedAndStable() {
        val task = YearTask(YearArea.MATH, YearTerm.FIRST, "덧셈", "매일 5문제")
        assertEquals("year:e1:MATH:덧셈", task.storageId("e1"))
        assertTrue(task.storageId("e1").startsWith(YearTask.PREFIX))
    }

    @Test
    fun infantsDoNothingAloneAndSchoolChildrenMostlyDoThemselves() {
        // 만 0세: 전부 부모가(또는 같이) 해 주는 일. "0세가 그림책을 읽는다" 같은 줄이 없어야 해요.
        assertTrue(YearPlans.forYear("a0").none { it.who == YearDoer.CHILD })
        assertTrue(YearPlans.forYear("a1").none { it.who == YearDoer.CHILD })
        assertTrue(YearPlans.forYear("a0").filter { it.area == YearArea.TALK }.all { it.who == YearDoer.PARENT })
        // 만 3세부터 "스스로"가 생기고, 학교에 가면 교과 공부는 대부분 스스로.
        assertTrue(YearPlans.forYear("a3").any { it.who == YearDoer.CHILD })
        val subjects = setOf(YearArea.KOREAN, YearArea.MATH, YearArea.ENGLISH, YearArea.SOCIETY, YearArea.SCIENCE, YearArea.READING)
        listOf("e3", "m1", "h1").forEach { key ->
            val study = YearPlans.forYear(key).filter { it.area in subjects && it.who != YearDoer.MENTOR }
            assertTrue(key, study.count { it.who == YearDoer.CHILD } * 2 > study.size)
        }
    }

    @Test
    fun schoolYearsFollowCurriculumTermsAndSleepGuidance() {
        fun task(key: String, title: String) = YearPlans.forYear(key).single { it.title == title }
        // 2022 개정: 초2 cm 는 1학기·곱셈구구는 2학기, 초3 분수·소수는 1학기, 초4 촌락은 2학기
        assertEquals(YearTerm.FIRST, task("e2", "길이 재기(cm)").term)
        assertEquals(YearTerm.SECOND, task("e2", "곱셈구구").term)
        assertEquals(YearTerm.FIRST, task("e3", "분수와 소수 처음").term)
        assertEquals(YearTerm.SECOND, task("e4", "촌락과 도시·경제 기초").term)
        // 창체는 3영역(봉사 영역 없음), 중학교 배정은 부모가
        assertTrue(YearPlans.forYear("h1").none { it.title.contains("봉사") })
        assertEquals(YearDoer.PARENT, task("e6", "중학교 배정 원서").who)
        // 수면은 권장(중·고 8~10시간) 아래로 적지 않아요
        listOf("m1", "m2", "m3", "h1", "h2", "h3").forEach { key ->
            val all = YearPlans.forYear(key).joinToString { it.title + it.how }
            assertFalse(key, all.contains("7시간"))
            assertTrue(key, all.contains("8시간") || all.contains("8~10시간"))
        }
    }

    @Test
    fun everyRoleHasItsShareAtTheRightAges() {
        val school = listOf("e1", "e2", "e3", "e4", "e5", "e6", "m1", "m2", "m3", "h1", "h2", "h3")
        // 멘토 몫은 학교부터, 학령 전에는 없어요.
        school.forEach { key -> assertTrue(key, YearPlans.forYear(key).any { it.who == YearDoer.MENTOR }) }
        listOf("a0", "a3", "a6").forEach { key -> assertTrue(key, YearPlans.forYear(key).none { it.who == YearDoer.MENTOR }) }
        // 부모 몫(지원·서류·상담)과 건강 체크리스트는 고3까지 해마다.
        (listOf("a0", "a1", "a2", "a3", "a4", "a5", "a6") + school).forEach { key ->
            val tasks = YearPlans.forYear(key)
            assertTrue(key, tasks.any { it.who == YearDoer.PARENT })
            assertTrue(key, tasks.any { it.area == YearArea.BODY })
            assertTrue(key, tasks.size >= 10)
        }
    }

    @Test
    fun healthChecklistFollowsNationalSchedules() {
        fun titles(key: String) = YearPlans.forYear(key).map { it.title }
        assertTrue(titles("a0").containsAll(listOf("BCG(결핵)", "영유아 검진 1차", "영유아 검진 2차", "영유아 검진 3차")))
        assertTrue(titles("a1").contains("MMR 1차·수두"))
        assertTrue(YearPlans.forYear("e6").any { it.how.contains("HPV") && it.how.contains("남아") })
        // 학생 건강검진은 초1·초4·중1·고1만, 정서·행동특성검사도 같은 학년
        listOf("e1", "e4", "m1", "h1").forEach { key ->
            assertTrue(key, titles(key).containsAll(listOf("학생 건강검진", "학생정서·행동특성검사")))
        }
        listOf("e2", "e3", "e5", "e6", "m2", "m3", "h2", "h3").forEach { key -> assertFalse(key, titles(key).contains("학생 건강검진")) }
        // 인플루엔자 무료 접종은 만 13세까지: 고등학생 목록엔 무료 문구가 없어요.
        listOf("h1", "h2", "h3").forEach { key -> assertFalse(key, YearPlans.forYear(key).any { it.how.contains("만 13세 무료") }) }
    }
}
