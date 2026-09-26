package com.nextstep.app.domain.selfdirection

import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class SelfDirectionTest {
    private val tuesday = LocalDate.of(2029, 3, 6)
    private val monday = LocalDate.of(2029, 3, 5)

    private fun plan(week: LocalDate, goals: String = "영어 책 3권", minutes: Int = 0, by: Role = Role.STUDENT, reflected: Boolean = false, done: Int = 0) =
        WeekPlanEntity(id = "w$week", familyId = Fixtures.FAMILY, weekStart = week.toEpochDay(), goals = goals, plannedMinutes = minutes, authorRole = by.name,
            reflectedAt = if (reflected) 1L else null, mood = if (reflected) 3 else 0, doneMask = done)

    @Test
    fun stagesHandTheLoopOverOneStepAtATimeAndNeverTakeItBack() {
        SelfDirectionStage.entries.zipWithNext().forEach { (a, b) ->
            LoopStep.entries.forEach { step -> assertTrue("$a→$b $step", b.owner(step) >= a.owner(step)) }
            assertTrue(b.childSteps >= a.childSteps)
        }
        assertEquals(0, SelfDirectionStage.FOLLOW.childSteps)
        assertEquals(Owner.ADULT, SelfDirectionStage.CHOOSE.owner(LoopStep.PLAN))
        assertEquals(Owner.TOGETHER, SelfDirectionStage.PLAN_TOGETHER.owner(LoopStep.PLAN))
        assertEquals(4, SelfDirectionStage.PLAN_FIRST.childSteps)
        assertTrue(SelfDirectionStage.PLAN_FIRST.needsApproval && SelfDirectionStage.entries.count { it.needsApproval } == 1)
        assertFalse(SelfDirectionStage.OWN.adultSeesDetails)
        assertNull(SelfDirectionStage.OWN.handOverNext)
        assertEquals(ReflectionForm.FACES, SelfDirectionStage.FOLLOW.reflection)
    }

    @Test
    fun defaultFollowsTheScreenLevelAndParentsCanChoose() {
        assertEquals(SelfDirectionStage.entries, StudentUiLevel.entries.map { SelfDirectionStage.defaultFor(it) })
        val kid = Fixtures.member(Role.STUDENT, "지우", gradeYear = 5)
        assertEquals(SelfDirectionStage.PLAN_FIRST, SelfDirection.stageOf(kid, tuesday))
        assertEquals(SelfDirectionStage.PLAN_TOGETHER, SelfDirection.stageOf(kid.copy(selfDirection = "PLAN_TOGETHER"), tuesday))
        assertEquals(SelfDirectionStage.OWN, SelfDirection.stageOf(null, tuesday))
    }

    @Test
    fun reflectOnFridayToSundayOrCatchUpOnLastWeek() {
        val fri = LocalDate.of(2029, 3, 9)
        assertEquals(monday, SelfDirection.reflectWeek(emptyList(), fri))
        assertNull(SelfDirection.reflectWeek(listOf(plan(monday, reflected = true)), fri))
        assertNull(SelfDirection.reflectWeek(emptyList(), tuesday))
        assertEquals(monday.minusWeeks(1), SelfDirection.reflectWeek(listOf(plan(monday.minusWeeks(1))), tuesday))
        assertNull(SelfDirection.reflectWeek(listOf(plan(monday.minusWeeks(1), reflected = true)), tuesday))
    }

    @Test
    fun weekStatusCountsThisWeeksMinutesAndApproval() {
        val sessions = listOf(
            Fixtures.session("math", monday, LocalTime.of(9, 0), 40), Fixtures.session("math", tuesday, LocalTime.of(9, 0), 20),
            Fixtures.session("math", monday.minusDays(1), LocalTime.of(9, 0), 90),
        )
        val w = SelfDirection.week(SelfDirectionStage.PLAN_FIRST, listOf(plan(monday, minutes = 120)), sessions, tuesday)
        assertEquals(monday, w.weekStart); assertEquals(60, w.actualMinutes); assertEquals(0.5f, w.keptRatio!!, 0.001f)
        assertTrue(w.hasPlan); assertTrue(w.waitingApproval)
        assertFalse(SelfDirection.week(SelfDirectionStage.PLAN_TOGETHER, listOf(plan(monday)), emptyList(), tuesday).waitingApproval)
        val last = SelfDirection.week(SelfDirectionStage.SELF, listOf(plan(monday.minusWeeks(1))), emptyList(), tuesday)
        assertFalse(last.hasPlan); assertTrue(last.reflectsLastWeek)
    }

    @Test
    fun evidenceLooksAtTheFourWeeksBeforeThisOne() {
        val plans = listOf(plan(monday), plan(monday.minusWeeks(1), by = Role.PARENT, reflected = true, done = 1), plan(monday.minusWeeks(2)))
        val e = SelfDirection.evidence(plans, emptyList(), tuesday)
        assertEquals(4, e.size); assertEquals(monday.minusWeeks(4), e.first().weekStart); assertEquals(monday.minusWeeks(1), e.last().weekStart)
        assertTrue(e.last().planned); assertFalse(e.last().childPlanned); assertTrue(e.last().reflected); assertEquals(1, e.last().goalsDone)
        assertTrue(e[2].childPlanned); assertFalse(e[0].planned)
    }

    @Test
    fun readinessSuggestsHandingOverOrStayingTogether() {
        fun weeks(n: Int, child: Boolean = true, reflected: Boolean = true, kept: Float? = null) =
            (1..4).map { i -> WeekEvidence(monday.minusWeeks(i.toLong()), planned = i <= n, childPlanned = child && i <= n, reflected = reflected && i <= n, goalsDone = 0, goalsTotal = 0, keptRatio = if (i <= n) kept else null) }
        assertEquals(SelfDirectionStage.CHOOSE, SelfDirection.suggest(SelfDirectionStage.FOLLOW, weeks(3, child = false), null)!!.to)
        assertNull(SelfDirection.suggest(SelfDirectionStage.CHOOSE, weeks(3, child = false), 0.1f))
        assertTrue(SelfDirection.suggest(SelfDirectionStage.CHOOSE, weeks(3, child = false), 0.3f)!!.up)
        assertEquals(SelfDirectionStage.PLAN_FIRST, SelfDirection.suggest(SelfDirectionStage.PLAN_TOGETHER, weeks(3), null)!!.to)
        assertNull(SelfDirection.suggest(SelfDirectionStage.PLAN_FIRST, weeks(3, kept = 0.6f), null))
        assertEquals(SelfDirectionStage.SELF, SelfDirection.suggest(SelfDirectionStage.PLAN_FIRST, weeks(4, kept = 0.9f), null)!!.to)
        val back = SelfDirection.suggest(SelfDirectionStage.SELF, weeks(1), null)!!
        assertFalse(back.up); assertEquals(SelfDirectionStage.PLAN_FIRST, back.to)
        assertFalse(SelfDirection.suggest(SelfDirectionStage.PLAN_FIRST, weeks(4, kept = 0.3f), null)!!.up)
        assertNull(SelfDirection.suggest(SelfDirectionStage.PLAN_TOGETHER, weeks(0), null)) // 같이 계획하는 단계는 되돌리지 않음
        assertNull(SelfDirection.suggest(SelfDirectionStage.OWN, weeks(4, kept = 1f), null))
    }

    @Test
    fun reportCombinesStageEvidenceAndSelfMadeTasks() {
        val kid = Fixtures.member(Role.STUDENT, "지우", gradeYear = 3).copy(selfDirection = "PLAN_FIRST")
        val tasks = listOf(Fixtures.task("a", tuesday, by = "STUDENT"), Fixtures.task("b", tuesday, by = "PARENT"))
        val r = SelfDirection.report(kid, emptyList(), emptyList(), tasks, tuesday)
        assertEquals(SelfDirectionStage.PLAN_FIRST, r.stage); assertEquals(SelfDirectionStage.PLAN_TOGETHER, r.defaultStage); assertTrue(r.chosen)
        assertEquals(0.5f, r.selfTaskRatio!!, 0.001f)
        assertFalse(r.suggestion!!.up) // 4주 모두 계획이 없어 잠깐 같이 하기
        assertEquals(listOf("a", "b", "c"), SelfDirection.cleanGoals(listOf(" a ", "", "b", "c", "d")))
    }
}
