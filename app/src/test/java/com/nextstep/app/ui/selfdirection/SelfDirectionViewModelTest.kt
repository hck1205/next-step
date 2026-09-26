package com.nextstep.app.ui.selfdirection

import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeMemberRepository
import com.nextstep.app.fake.FakeWeekPlanRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SelfDirectionViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val weekPlans = FakeWeekPlanRepository(streams)
    private val members = FakeMemberRepository()
    private val today = LocalDate.of(2029, 3, 7) // 수요일, 이번 주 월요일 = 3월 5일
    private val monday = LocalDate.of(2029, 3, 5)

    private fun vm() = SelfDirectionViewModel(streams, weekPlans, members, today = { today })

    private fun childPlanned(week: LocalDate) = WeekPlanEntity(
        id = "w$week", familyId = Fixtures.FAMILY, weekStart = week.toEpochDay(), goals = "리더스 5권", authorRole = Role.STUDENT.name, reflectedAt = 1L, mood = 3, good = "매일 읽음",
    )

    @Test
    fun readyChildGetsAHandOverSuggestionAndHistory() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 3))
        streams.weekPlans.value = (1..3).map { childPlanned(monday.minusWeeks(it.toLong())) } + childPlanned(monday)
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        val r = s.report!!
        assertEquals(SelfDirectionStage.PLAN_TOGETHER, r.stage); assertFalse(r.chosen); assertEquals(3, r.childPlannedWeeks)
        assertTrue(r.suggestion!!.up); assertEquals(SelfDirectionStage.PLAN_FIRST, r.suggestion!!.to)
        assertEquals(3, s.history.size); assertEquals(monday.minusWeeks(1).toEpochDay(), s.history.first().weekStart)
        assertTrue(s.week!!.hasPlan)
        job.cancel()
    }

    @Test
    fun parentMovesTheStageAndBackToDefault() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 3))
        val vm = vm(); val job = subscribe(vm.state)
        settle(vm.state)
        vm.onEvent(SelfDirectionEvent.SetStage(SelfDirectionStage.PLAN_FIRST)); settle(vm.state)
        vm.onEvent(SelfDirectionEvent.SetStage(SelfDirectionStage.PLAN_TOGETHER)); settle(vm.state)
        vm.onEvent(SelfDirectionEvent.SetStage(null)); settle(vm.state)
        assertEquals(listOf("self:kid:PLAN_FIRST", "self:kid:null", "self:kid:null"), members.calls)
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 3).copy(selfDirection = "PLAN_FIRST"))
        assertTrue(settle(vm.state).report!!.chosen)
        job.cancel()
    }

    @Test
    fun planToggleApproveAndReflectGoToTheRepository() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 5))
        val vm = vm(); val job = subscribe(vm.state)
        settle(vm.state)
        vm.onEvent(SelfDirectionEvent.SavePlan(listOf("a"), 120))
        vm.onEvent(SelfDirectionEvent.ToggleGoal("x", 0))
        vm.onEvent(SelfDirectionEvent.Approve("x"))
        vm.onEvent(SelfDirectionEvent.Reflect(monday, 2, "g", "h", "c")); settle(vm.state)
        assertEquals(listOf("plan:$monday:a:120", "toggle:x:0", "approve:x", "reflect:$monday:2:g:h:c"), weekPlans.calls)
        job.cancel()
    }
}
