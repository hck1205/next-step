package com.nextstep.app.domain.growth

import com.nextstep.app.data.model.Role
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StudentScreenTest {
    private val today = LocalDate.of(2026, 9, 24)
    private fun student(gradeYear: Int) = Fixtures.member(Role.STUDENT, "나", gradeYear = gradeYear)

    @Test
    fun withoutStudentTheFullScreenIsUsed() {
        val s = StudentScreen.of(null, today)
        assertEquals(StudentUiLevel.TREE, s.level); assertNull(s.year); assertEquals(1f, s.textScale, 0f)
        assertEquals(StudentHomeSection.TIMER, s.homeOrder.first())
    }

    @Test
    fun everyYearGetsItsOwnTextSizeRowsAndCardOrder() {
        val screens = (1..12).map { StudentScreen.of(student(it), today) }
        assertEquals(12, screens.map { it.year!!.key }.toSet().size)
        assertEquals(1.3f, screens[0].textScale, 0f); assertEquals(1.25f, screens[1].textScale, 0f); assertEquals(1.2f, screens[2].textScale, 0f)
        assertEquals(2, screens[1].taskRows); assertEquals(3, screens[2].taskRows)
        // 초2: 타이머 다음 올해의 공부, 중2: 타이머 다음 시험·목표, 고3: 시험·목표 다음 학습 계획
        assertEquals(listOf(StudentHomeSection.TIMER, StudentHomeSection.YEAR), screens[1].homeOrder.take(2))
        assertEquals(listOf(StudentHomeSection.TIMER, StudentHomeSection.MISSION), screens[7].homeOrder.take(2))
        assertEquals(listOf(StudentHomeSection.TIMER, StudentHomeSection.MISSION, StudentHomeSection.PLANNER), screens[11].homeOrder.take(3))
        screens.forEach { s -> assertTrue(s.homeOrder.all(s.level::shows)); assertEquals(s.homeOrder.size, s.homeOrder.toSet().size) }
    }

    @Test
    fun preschoolersHaveNoTimerAndTasksComeFirst() {
        val s = StudentScreen.of(Fixtures.member(Role.STUDENT, "아기", birthDate = LocalDate.of(2022, 3, 1)), today)
        assertEquals("a4", s.year!!.key); assertEquals(StudentUiLevel.SEED, s.level); assertEquals(1.4f, s.textScale, 0f)
        assertEquals(listOf(StudentHomeSection.TASKS, StudentHomeSection.YEAR, StudentHomeSection.WEEK, StudentHomeSection.NOTE), s.homeOrder)
    }

    @Test
    fun parentsChoiceOverridesTheYearsTextAndRows() {
        val s = StudentScreen.of(student(8).copy(uiLevel = "SPROUT"), today)
        assertEquals(StudentUiLevel.SPROUT, s.level); assertEquals("m2", s.year!!.key)
        assertEquals(StudentUiLevel.SPROUT.textScale, s.textScale, 0f); assertEquals(StudentUiLevel.SPROUT.taskRows, s.taskRows)
        assertTrue(StudentHomeSection.MISSION !in s.homeOrder)
    }
}
