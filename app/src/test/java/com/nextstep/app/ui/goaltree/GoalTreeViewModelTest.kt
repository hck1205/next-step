package com.nextstep.app.ui.goaltree

import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGoalRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalTreeViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val goals = FakeGoalRepository()
    private val today = LocalDate.of(2029, 5, 10)

    private fun vm() = GoalTreeViewModel(streams, goals, today = { today })
    private fun tree(id: String, leadsTo: String? = null, status: GoalStatus = GoalStatus.ACTIVE, area: GoalArea = GoalArea.LANGUAGE) =
        Fixtures.goal("목표$id", trackId = GoalTree.TRACK, id = id, status = status, area = area.name).copy(leadsTo = leadsTo)

    @Test
    fun treeShowsRootsWithChildrenAndFilters() = runTest {
        streams.goals.value = listOf(tree("big"), tree("small", leadsTo = "big"), tree("done", status = GoalStatus.DONE), tree("math", area = GoalArea.MATH), Fixtures.goal("트랙", trackId = "x"))
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals(setOf("big", "math"), s.roots.map { it.goal.id }.toSet()); assertEquals(listOf("small"), s.childrenOf("big").map { it.goal.id })
        assertEquals(3, s.count(GoalFilter.ACTIVE)); assertEquals(1, s.count(GoalFilter.DONE))
        vm.onEvent(GoalTreeEvent.SetFilter(GoalFilter.DONE)); s = settle(vm.state)
        assertEquals(listOf("done"), s.roots.map { it.goal.id })
        vm.onEvent(GoalTreeEvent.SetFilter(GoalFilter.ACTIVE)); vm.onEvent(GoalTreeEvent.SetArea(GoalArea.MATH)); s = settle(vm.state)
        assertEquals(listOf("math"), s.roots.map { it.goal.id })
        assertEquals(listOf(GoalArea.LANGUAGE, GoalArea.MATH), s.areas)
        job.cancel()
    }

    @Test
    fun anyoneCanCreateAGoalLinkedToABiggerOne() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        settle(vm.state)
        vm.onEvent(GoalTreeEvent.Create("  ", "", GoalArea.HABIT, null, null, "PARENT"))
        vm.onEvent(GoalTreeEvent.Create("영어 일기", "쓰기 자신감", GoalArea.LANGUAGE, today.plusDays(30), "big", "MENTOR")); settle(vm.state)
        val g = goals.addedGoals.single()
        assertTrue(GoalTree.isTreeGoal(g)); assertEquals("big", g.leadsTo); assertEquals("MENTOR", g.createdByRole); assertEquals(today.plusDays(30).toEpochDay(), g.targetDate)
        job.cancel()
    }

    @Test
    fun cardsShowTheRewardWaitingOnEachGoal() = runTest {
        streams.goals.value = listOf(tree("big"), tree("small", leadsTo = "big"))
        streams.rewards.value = listOf(
            com.nextstep.app.data.local.entity.RewardEntity(id = "r1", familyId = Fixtures.FAMILY, kind = "GOAL", targetId = "big", title = "나들이"),
            com.nextstep.app.data.local.entity.RewardEntity(id = "r2", familyId = Fixtures.FAMILY, kind = "GOAL", targetId = "small", title = "받은 것", givenAt = 1L),
        )
        val vm = vm(); val job = subscribe(vm.state)
        assertEquals(mapOf("big" to "나들이"), settle(vm.state).rewardTitles)
        job.cancel()
    }
}
