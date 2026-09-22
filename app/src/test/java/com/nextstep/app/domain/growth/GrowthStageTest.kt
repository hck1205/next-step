package com.nextstep.app.domain.growth

import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.model.Role
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GrowthStageTest {
    @Test
    fun gradeYearMapsToStageAndLabel() {
        assertEquals(GrowthStage.EARLY_ELEMENTARY, GrowthStage.fromGradeYear(1))
        assertEquals(GrowthStage.UPPER_ELEMENTARY, GrowthStage.fromGradeYear(6))
        assertEquals(GrowthStage.MIDDLE, GrowthStage.fromGradeYear(7))
        assertEquals(GrowthStage.HIGH, GrowthStage.fromGradeYear(12))
        assertEquals("초3", GrowthStage.EARLY_ELEMENTARY.gradeLabel(3))
        assertEquals("중2", GrowthStage.MIDDLE.gradeLabel(8))
        assertEquals("고3", GrowthStage.HIGH.gradeLabel(12))
    }

    @Test
    fun unknownGradeHasNoStageAndHigherEducationMaps() {
        assertNull(GrowthStage.fromGradeYear(0)); assertNull(GrowthStage.fromGradeYear(19)); assertNull(GrowthStage.fromGradeYear(null))
        assertEquals(GrowthStage.UNIVERSITY, GrowthStage.fromGradeYear(13)); assertEquals(GrowthStage.GRADUATE, GrowthStage.fromGradeYear(18))
        assertEquals("대2", GrowthStage.UNIVERSITY.gradeLabel(14)); assertEquals("대학원 1년차", GrowthStage.GRADUATE.gradeLabel(17))
    }

    @Test
    fun birthDateDecidesPreschoolStagesByMonths() {
        val today = LocalDate.of(2026, 9, 22)
        assertEquals(GrowthStage.NEWBORN, GrowthStage.fromBirthDate(LocalDate.of(2026, 3, 1), today))
        assertEquals(GrowthStage.TODDLER, GrowthStage.fromBirthDate(LocalDate.of(2025, 9, 1), today))
        assertEquals(GrowthStage.PRESCHOOL, GrowthStage.fromBirthDate(LocalDate.of(2022, 6, 1), today))
        assertNull(GrowthStage.fromBirthDate(LocalDate.of(2027, 1, 1), today))
        assertEquals("만 1세 0개월", GrowthStage.ageLabel(LocalDate.of(2025, 9, 22), today))
        assertEquals("출생 전", GrowthStage.ageLabel(LocalDate.of(2027, 1, 1), today))
    }

    @Test
    fun schoolGradeFollowsKoreanCalendarWithMarchStart() {
        // 2019년생: 2026년 3월 초등 입학 → 2026-09 은 초1, 2027-02 는 아직 초1, 2027-03 은 초2
        val born = LocalDate.of(2019, 7, 1)
        assertEquals(1, GrowthStage.schoolGradeYear(born, LocalDate.of(2026, 9, 22)))
        assertEquals(1, GrowthStage.schoolGradeYear(born, LocalDate.of(2027, 2, 28)))
        assertEquals(2, GrowthStage.schoolGradeYear(born, LocalDate.of(2027, 3, 1)))
        assertNull(GrowthStage.schoolGradeYear(born, LocalDate.of(2026, 2, 1)))
        assertEquals(GrowthStage.PRESCHOOL, GrowthStage.fromBirthDate(born, LocalDate.of(2026, 2, 1)))
        assertEquals(GrowthStage.HIGH, GrowthStage.fromBirthDate(LocalDate.of(2010, 1, 1), LocalDate.of(2026, 9, 22)))
        assertEquals(GrowthStage.UNIVERSITY, GrowthStage.fromBirthDate(LocalDate.of(2006, 5, 5), LocalDate.of(2026, 9, 22)))
        assertNull(GrowthStage.schoolGradeYear(LocalDate.of(1990, 1, 1), LocalDate.of(2026, 9, 22)))
    }

    @Test
    fun birthDateWinsOverGradeYearForMembers() {
        val today = LocalDate.of(2026, 9, 22)
        val student = Fixtures.member(Role.STUDENT, "나", gradeYear = 12, birthDate = LocalDate.of(2024, 1, 1))
        assertEquals(GrowthStage.TODDLER, GrowthStage.of(listOf(student), today))
        assertEquals(GrowthStage.HIGH, GrowthStage.of(listOf(student.copy(birthDate = null)), today))
    }

    @Test
    fun stageComesFromStudentMemberOnly() {
        val members = listOf(Fixtures.member(Role.PARENT, "엄마").copy(gradeYear = 12), Fixtures.member(Role.STUDENT, "나").copy(gradeYear = 5))
        assertEquals(GrowthStage.UPPER_ELEMENTARY, GrowthStage.of(members))
        assertNull(GrowthStage.of(listOf(Fixtures.member(Role.STUDENT, "나"))))
        assertNull(GrowthStage.of(emptyList()))
    }

    @Test
    fun gradeOptionsCoverSchoolThroughGraduateInOrder() {
        val options = GrowthStage.gradeOptions()
        assertEquals(18, options.size)
        assertEquals(listOf("초1", "초2", "초3", "초4", "초5", "초6", "중1", "중2", "중3", "고1", "고2", "고3", "대1", "대2", "대3", "대4", "대학원 1년차", "대학원 2년차"), options.map { it.second })
    }

    @Test
    fun youngerStagesHaveShorterSessionsAndElementaryContentLevel() {
        val stages = GrowthStage.entries
        assertTrue(stages.zipWithNext().all { (a, b) -> a.sessionMinutes <= b.sessionMinutes })
        assertTrue(stages.filter { it.isSchoolAge }.zipWithNext().all { (a, b) -> a.sessionMinutes < b.sessionMinutes })
        assertEquals(0, GrowthStage.NEWBORN.sessionMinutes); assertEquals(0, GrowthStage.TODDLER.sessionsPerDay)
        assertEquals(GradeLevel.ELEMENTARY, GrowthStage.EARLY_ELEMENTARY.gradeLevel)
        assertEquals(GradeLevel.HIGH, GrowthStage.HIGH.gradeLevel)
    }

    @Test
    fun guideExistsForEveryStageWithAllSections() {
        GrowthStage.entries.forEach { stage ->
            val g = GrowthGuide.forStage(stage)
            assertEquals(stage, g.stage)
            assertTrue(g.inputPrinciple.isNotBlank()); assertTrue(g.parentTips.size >= 3); assertTrue(g.mentorTips.size >= 3)
            assertTrue(g.experiences.size >= 5); assertTrue(g.praiseStyle.isNotBlank())
            assertEquals(stage.sessionMinutes, g.planOptions.sessionMinutes); assertEquals(stage.sessionsPerDay, g.planOptions.sessionsPerDay)
        }
        assertEquals(false, GrowthGuide.forStage(GrowthStage.EARLY_ELEMENTARY).planOptions.includeWeekend)
    }

    @Test
    fun pickForDayIsStableWithinADayAndCyclesAcrossDays() {
        val items = listOf("a", "b", "c")
        val day = LocalDate.of(2026, 9, 22)
        assertEquals(GrowthGuide.pickForDay(items, day), GrowthGuide.pickForDay(items, day))
        val picks = (0 until 3).map { GrowthGuide.pickForDay(items, day.plusDays(it.toLong())) }
        assertEquals(items.toSet(), picks.toSet())
        assertNull(GrowthGuide.pickForDay(emptyList<String>(), day))
    }

    @Test
    fun defaultPlanOptionsFallBackWhenStageUnknown() {
        assertEquals(com.nextstep.app.domain.planner.PlanOptions(), GrowthGuide.defaultPlanOptions(null))
        assertEquals(60, GrowthGuide.defaultPlanOptions(GrowthStage.HIGH).sessionMinutes)
    }
}
