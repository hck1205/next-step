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
    fun unknownGradeHasNoStage() {
        assertNull(GrowthStage.fromGradeYear(0)); assertNull(GrowthStage.fromGradeYear(13)); assertNull(GrowthStage.fromGradeYear(null))
    }

    @Test
    fun stageComesFromStudentMemberOnly() {
        val members = listOf(Fixtures.member(Role.PARENT, "엄마").copy(gradeYear = 12), Fixtures.member(Role.STUDENT, "나").copy(gradeYear = 5))
        assertEquals(GrowthStage.UPPER_ELEMENTARY, GrowthStage.of(members))
        assertNull(GrowthStage.of(listOf(Fixtures.member(Role.STUDENT, "나"))))
        assertNull(GrowthStage.of(emptyList()))
    }

    @Test
    fun gradeOptionsCoverAllTwelveYearsInOrder() {
        val options = GrowthStage.gradeOptions()
        assertEquals(12, options.size)
        assertEquals(listOf("초1", "초2", "초3", "초4", "초5", "초6", "중1", "중2", "중3", "고1", "고2", "고3"), options.map { it.second })
    }

    @Test
    fun youngerStagesHaveShorterSessionsAndElementaryContentLevel() {
        val stages = GrowthStage.entries
        assertTrue(stages.zipWithNext().all { (a, b) -> a.sessionMinutes < b.sessionMinutes })
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
