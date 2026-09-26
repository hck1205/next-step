package com.nextstep.app.domain.growth

import com.nextstep.app.data.model.Role
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StudentUiLevelTest {
    private val today = LocalDate.of(2026, 9, 24)
    private fun student(gradeYear: Int = 0, birth: LocalDate? = null) = Fixtures.member(Role.STUDENT, "나", gradeYear = gradeYear, birthDate = birth)

    @Test
    fun gradesMapToLevelsTwoYearsAtATimeThenBySchool() {
        val byGrade = (0..18).associateWith { StudentUiLevel.forGrade(it) }
        assertEquals(StudentUiLevel.SEED, byGrade[0]); assertEquals(StudentUiLevel.SPROUT, byGrade[1]); assertEquals(StudentUiLevel.SPROUT, byGrade[2])
        assertEquals(StudentUiLevel.SEEDLING, byGrade[3]); assertEquals(StudentUiLevel.SEEDLING, byGrade[4])
        assertEquals(StudentUiLevel.STEM, byGrade[5]); assertEquals(StudentUiLevel.STEM, byGrade[6])
        assertEquals(StudentUiLevel.BRANCH, byGrade[7]); assertEquals(StudentUiLevel.BRANCH, byGrade[9])
        assertEquals(StudentUiLevel.TREE, byGrade[10]); assertEquals(StudentUiLevel.TREE, byGrade[18])
    }

    @Test
    fun screenOnlyGrowsWithLevel() {
        StudentUiLevel.entries.zipWithNext().forEach { (younger, older) ->
            assertTrue(older.sections.containsAll(younger.sections))
            assertTrue(older.textScale <= younger.textScale)
            assertTrue(older.touchTargetDp <= younger.touchTargetDp)
            assertTrue(older.recordChoices >= younger.recordChoices)
        }
        assertEquals(StudentHomeSection.entries.toSet(), StudentUiLevel.TREE.sections)
        assertTrue(StudentUiLevel.entries.all { it.taskRows <= 3 && it.touchTargetDp >= 48 })
        assertEquals(setOf(StudentHomeSection.TIMER, StudentHomeSection.TASKS, StudentHomeSection.MY_WEEK, StudentHomeSection.ROUTINE, StudentHomeSection.WEEK, StudentHomeSection.YEAR), StudentUiLevel.SPROUT.sections)
        assertTrue(!StudentUiLevel.SEEDLING.showsNumbers && StudentUiLevel.STEM.showsNumbers)
        assertEquals(StudentWords.EASY, StudentUiLevel.SEEDLING.words); assertEquals(StudentWords.STANDARD, StudentUiLevel.STEM.words)
        // 가장 어린 단계: 타이머 없이 할 일·별·가족 한마디만, 가장 큰 글씨
        assertEquals(setOf(StudentHomeSection.TASKS, StudentHomeSection.MY_WEEK, StudentHomeSection.ROUTINE, StudentHomeSection.WEEK, StudentHomeSection.YEAR), StudentUiLevel.SEED.sections)
        assertTrue(!StudentUiLevel.SEED.shows(StudentHomeSection.TIMER)); assertEquals(StudentUiLevel.entries.first(), StudentUiLevel.SEED)
        assertEquals(listOf(StudentHomeSection.TIMER), StudentUiLevel.SPROUT.newSince(StudentUiLevel.SEED))
    }

    @Test
    fun newSinceListsOnlyOpenedCardsInScreenOrder() {
        assertEquals(listOf(StudentHomeSection.EVENTS, StudentHomeSection.REVIEW, StudentHomeSection.RECOMMENDATION), StudentUiLevel.SEEDLING.newSince(StudentUiLevel.SPROUT))
        assertEquals(StudentUiLevel.SEEDLING.opens + StudentUiLevel.STEM.opens, StudentUiLevel.STEM.newSince(StudentUiLevel.SPROUT).toSet())
        assertTrue(StudentUiLevel.TREE.newSince(StudentUiLevel.TREE).isEmpty())
        assertTrue(StudentUiLevel.SPROUT.newSince(StudentUiLevel.TREE).isEmpty())
    }

    @Test
    fun autoUsesBirthDateThenGradeThenFullScreen() {
        assertEquals(StudentUiLevel.SPROUT, StudentUiLevel.auto(student(birth = LocalDate.of(2019, 5, 1)), today)) // 초1
        assertEquals(StudentUiLevel.SEEDLING, StudentUiLevel.auto(student(gradeYear = 9, birth = LocalDate.of(2017, 5, 1)), today)) // 생년월일(초3)이 학년보다 우선
        assertEquals(StudentUiLevel.SEED, StudentUiLevel.auto(student(birth = LocalDate.of(2022, 1, 1)), today)) // 학령 전
        assertEquals(StudentUiLevel.SEED, StudentUiLevel.auto(student(birth = LocalDate.of(2026, 3, 1)), today)) // 갓난아기도 씨앗
        assertEquals(StudentUiLevel.TREE, StudentUiLevel.auto(student(birth = LocalDate.of(1990, 1, 1)), today)) // 대학원 이후
        assertEquals(StudentUiLevel.BRANCH, StudentUiLevel.auto(student(gradeYear = 8), today))
        assertEquals(StudentUiLevel.TREE, StudentUiLevel.auto(student(), today))
        // 새 학년(3월)이 되면 저절로 다음 단계
        assertEquals(StudentUiLevel.SEEDLING, StudentUiLevel.auto(student(birth = LocalDate.of(2017, 5, 1)), LocalDate.of(2026, 3, 2)))
        assertEquals(StudentUiLevel.SPROUT, StudentUiLevel.auto(student(birth = LocalDate.of(2017, 5, 1)), LocalDate.of(2026, 2, 27)))
    }

    @Test
    fun parentChoiceOverridesAutoAndUnknownNamesFallBack() {
        assertEquals(StudentUiLevel.SPROUT, StudentUiLevel.of(student(gradeYear = 8).copy(uiLevel = "SPROUT"), today))
        assertEquals(StudentUiLevel.BRANCH, StudentUiLevel.of(student(gradeYear = 8).copy(uiLevel = "unknown"), today))
        assertEquals(null, StudentUiLevel.fromName(""))
    }

    @Test
    fun gradeSpanLabels() {
        assertEquals(listOf("학령 전", "초1–초2", "초3–초4", "초5–초6", "중1–중3", "고1부터"), StudentUiLevel.entries.map { it.gradeSpan })
    }
}
