package com.nextstep.app.domain.project

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectCatalogTest {
    private val plans = ProjectCatalog.plans

    @Test
    fun idsAndPhaseKeysAreUnique() {
        assertEquals(plans.size, plans.map { it.id }.toSet().size)
        plans.forEach { plan -> assertEquals(plan.id, plan.phases.size, plan.phases.map { it.key }.toSet().size) }
    }

    @Test
    fun everyCategoryHasAPlan() {
        ProjectCategory.entries.forEach { assertTrue(it.name, ProjectCatalog.byCategory(it).isNotEmpty()) }
    }

    @Test
    fun phasesStartInAgeOrderAndDailyAmountNeverShrinks() {
        plans.forEach { plan ->
            plan.phases.zipWithNext().forEach { (a, b) ->
                assertTrue("${plan.id} ${b.key} 나이", b.fromMonths > a.fromMonths)
                assertTrue("${plan.id} ${b.key} 양", b.dailyMinutes >= a.dailyMinutes)
            }
            assertTrue(plan.id, plan.maxDailyMinutes > plan.minDailyMinutes)
        }
    }

    @Test
    fun everyPhaseIsConcrete() {
        plans.flatMap { it.phases }.forEach { phase ->
            assertTrue(phase.key, phase.routine.isNotEmpty())
            assertTrue(phase.key, phase.checkpoint.isNotBlank() && phase.materials.isNotBlank())
            assertTrue(phase.key, phase.weeks > 0 && phase.routine.all { it.minutes > 0 && it.daysPerWeek in 1..7 })
        }
    }

    @Test
    fun preschoolPhasesStayShortAndPlayful() {
        plans.flatMap { it.phases }.filter { it.fromMonths < FIRST_GRADE_MONTHS }.forEach { phase ->
            assertTrue("${phase.title} 한 번", phase.routine.all { it.minutes <= PRESCHOOL_SESSION })
            assertTrue("${phase.title} 하루", phase.dailyMinutes <= PRESCHOOL_DAILY)
        }
    }

    @Test
    fun englishGrowsFromSongsToChapterBooksOverTenSteps() {
        val english = ProjectCatalog.byId.getValue("english-reader")
        assertEquals(10, english.phases.size)
        assertEquals(36, english.startMonths)
        assertEquals(RoutineKind.PLAY, english.phases.first().routine.single().kind)
        assertTrue(english.phases.last().routine.any { it.kind == RoutineKind.WRITE })
        assertTrue(english.totalHours >= 1000)
        assertEquals("매일 10분", RoutineItem("x", RoutineKind.READ, 10, 7).amountLabel)
        assertEquals("주 5일 · 10분", english.phases.first().routine.single().amountLabel)
    }

    private companion object {
        const val FIRST_GRADE_MONTHS = 78
        const val PRESCHOOL_SESSION = 15
        const val PRESCHOOL_DAILY = 20
    }
}
