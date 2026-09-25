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
        // 영아기(만 0~2세)는 말·놀이·생활·몸만.
        listOf("a0", "a1", "a2").forEach { key ->
            assertEquals(key, listOf(YearArea.TALK, YearArea.PLAY, YearArea.LIFE, YearArea.BODY), YearPlans.areasOf(key))
        }
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
}
