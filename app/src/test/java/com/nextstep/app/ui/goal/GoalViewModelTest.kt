package com.nextstep.app.ui.goal

import androidx.lifecycle.SavedStateHandle
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.goaltree.HistoryKind
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGoalRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.fake.FakeRewardRepository
import com.nextstep.app.domain.reward.RewardStatus
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val goals = FakeGoalRepository()
    private val tasks = FakeTaskRepository(streams)
    private val rewards = FakeRewardRepository(streams)
    private val today = LocalDate.of(2029, 5, 10)

    private fun vm(id: String = "mid") = GoalViewModel(SavedStateHandle(mapOf("goalId" to id)), streams, goals, tasks, rewards, today = { today })
    private fun tree(id: String, leadsTo: String? = null, status: GoalStatus = GoalStatus.ACTIVE) =
        Fixtures.goal("목표$id", trackId = GoalTree.TRACK, id = id, status = status).copy(leadsTo = leadsTo, createdByRole = "PARENT")

    private fun seed() {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 5))
        streams.goals.value = listOf(tree("top"), tree("mid", leadsTo = "top"), tree("leaf", leadsTo = "mid", status = GoalStatus.DONE), tree("other"))
    }

    @Test
    fun unknownOrNonTreeGoalShowsNothing() = runTest {
        streams.goals.value = listOf(Fixtures.goal("트랙", trackId = "x", id = "x"))
        val vm = vm("x"); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.loaded); assertNull(s.node)
        job.cancel()
    }

    @Test
    fun givingTasksRaisesTheRateAndTheChainShowsProgress() = runTest {
        seed()
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals(listOf("top"), s.chain.map { it.goal.id }); assertEquals(listOf("leaf"), s.children.map { it.goal.id })
        assertEquals(listOf("top", "other"), s.linkTargets.map { it.id }) // 자기 아래(leaf)와 자신은 이어질 수 없음
        assertEquals(SelfDirectionStage.PLAN_FIRST, s.stage)
        assertEquals(1f, s.node!!.rate, 0f) // 작은 목표 하나를 이뤘고 할 일은 없음
        vm.onEvent(GoalEvent.AddTask("영어 단어 20개", "eng", TaskType.HOMEWORK, today.plusDays(2), "PARENT")); s = settle(vm.state)
        val t = s.node!!.tasks.single()
        assertEquals("mid", t.goalId); assertEquals("PARENT", t.createdByRole); assertEquals("목표mid", t.note)
        assertEquals(0.5f, s.node!!.rate, 0.001f)
        assertEquals(0f, s.chain.single().rate, 0f) // top 은 작은 목표 mid 를 아직 이루지 않음
        vm.onEvent(GoalEvent.ToggleTask(t.id, true)); s = settle(vm.state)
        assertTrue(s.node!!.readyToAchieve)
        assertTrue(s.history.any { it.kind == HistoryKind.TASK_DONE && it.goalTitle == "목표mid" })
        vm.onEvent(GoalEvent.Achieve); settle(vm.state)
        assertEquals("goalStatus:mid:${GoalStatus.DONE}", goals.calls.last())
        job.cancel()
    }

    @Test
    fun linkEditChildAndNextGoal() = runTest {
        seed()
        val vm = vm(); val job = subscribe(vm.state)
        settle(vm.state)
        vm.onEvent(GoalEvent.Link("other"))
        vm.onEvent(GoalEvent.Edit("새 제목", "이유", null))
        vm.onEvent(GoalEvent.AddChild("작은 것", "", GoalArea.MATH, null, "STUDENT"))
        vm.onEvent(GoalEvent.AddNext("다음 것", "", GoalArea.MATH, null, "PARENT"))
        vm.onEvent(GoalEvent.Archive); settle(vm.state)
        assertEquals(listOf("link:mid:other", "edit:mid:새 제목:이유:null"), goals.calls.take(2))
        assertEquals(listOf("mid", "top"), goals.addedGoals.map { it.leadsTo })
        assertEquals("goalStatus:mid:${GoalStatus.ARCHIVED}", goals.calls.last())
        job.cancel()
    }

    @Test
    fun rewardOnAGoalCanBePromisedChangedGivenOrCancelled() = runTest {
        seed()
        val vm = vm(); val job = subscribe(vm.state)
        assertNull(settle(vm.state).reward)
        vm.onEvent(GoalEvent.PromiseReward("보드게임")); vm.onEvent(GoalEvent.PromiseReward("영화 보기"))
        var s = settle(vm.state)
        assertEquals("영화 보기", s.reward!!.reward.title); assertEquals(RewardStatus.PROMISED, s.reward!!.status)
        assertEquals(1, streams.rewards.value.size) // 아직 안 준 약속은 바뀜
        streams.goals.value = streams.goals.value.map { if (it.id == "mid") it.copy(status = GoalStatus.DONE) else it }
        s = settle(vm.state)
        assertEquals(RewardStatus.EARNED, s.reward!!.status)
        vm.onEvent(GoalEvent.GiveReward(s.reward!!.reward.id)); s = settle(vm.state)
        assertEquals(RewardStatus.GIVEN, s.reward!!.status)
        vm.onEvent(GoalEvent.CancelReward(s.reward!!.reward.id))
        assertNull(settle(vm.state).reward)
        assertEquals(listOf("promise:GOAL:mid:보드게임", "promise:GOAL:mid:영화 보기"), rewards.calls.take(2))
        job.cancel()
    }
}
