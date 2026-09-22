package com.nextstep.app.ui.goals

import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.journey.GoalTrackCatalog
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGoalRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalsViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val goals = FakeGoalRepository()
    private val tasks = FakeTaskRepository()
    private val born = LocalDate.of(2020, 5, 15) // 2027-03 초1 입학 → 2029-09-22 는 초3 2학기
    private val today = LocalDate.of(2029, 9, 22)

    private fun vm() = GoalsViewModel(streams, goals, tasks, today = { today })
    private fun withChild() { streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아이", id = "kid", birthDate = born)) }

    @Test
    fun withoutBirthDateNothingIsAvailable() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.loaded); assertFalse(s.hasBirthDate); assertTrue(s.availableTracks.isEmpty()); assertTrue(s.periods.isEmpty())
        job.cancel()
    }

    @Test
    fun startTrackCreatesGoalWithStepsForRemainingCalendarAndHidesTrack() = runTest {
        withChild()
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals("g3s2", s.currentPeriodKey); assertEquals("초3 2학기", s.currentPeriodLabel)
        assertTrue(s.availableTracks.any { it.id == "math-elementary" }); assertFalse(s.availableTracks.any { it.id == "mother-tongue" })
        vm.onEvent(GoalsEvent.StartTrack("math-elementary")); settle(vm.state)
        assertEquals(listOf("add:math-elementary:12"), goals.calls)
        assertEquals(GoalTrackCatalog.byId.getValue("math-elementary").title, goals.addedGoals.single().title)
        // 저장소가 반영되면 트랙은 목록에서 빠지고 목표는 진행 중으로
        streams.goals.value = goals.addedGoals.toList(); streams.goalSteps.value = goals.addedSteps.toList()
        s = settle(vm.state)
        assertFalse(s.availableTracks.any { it.id == "math-elementary" })
        val view = s.active.single()
        assertEquals(12, view.steps.size); assertEquals(0f, view.progress, 0f)
        assertEquals(listOf("g1s1", "g1s2", "g2s1", "g2s2", "g3s1", "g3s2"), view.currentSteps.map { it.periodKey })
        vm.onEvent(GoalsEvent.StartTrack("math-elementary")); vm.onEvent(GoalsEvent.StartTrack("nope")); settle(vm.state)
        assertEquals(1, goals.calls.size)
        job.cancel()
    }

    @Test
    fun customGoalKeepsOnlyFilledStepsAndAddStepAppends() = runTest {
        withChild()
        val vm = vm(); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(GoalsEvent.AddCustomGoal("피아노", GoalArea.EXPERIENCE, "d", listOf("g3s2" to "체르니 100", "g4s1" to " ", "g4s2" to "체르니 30")))
        vm.onEvent(GoalsEvent.AddCustomGoal("  ", GoalArea.CUSTOM, "", emptyList()))
        settle(vm.state)
        assertEquals(listOf("add:피아노:2"), goals.calls)
        assertEquals(listOf(0, 1), goals.addedSteps.map { it.orderIndex }); assertEquals(listOf("g3s2", "g4s2"), goals.addedSteps.map { it.periodKey })
        streams.goals.value = goals.addedGoals.toList(); streams.goalSteps.value = goals.addedSteps.toList()
        settle(vm.state)
        vm.onEvent(GoalsEvent.AddStep(goals.addedGoals.single().id, "g5s1", "체르니 40")); settle(vm.state)
        assertEquals(2, goals.addedSteps.last().orderIndex)
        job.cancel()
    }

    @Test
    fun finishingLastStepMarksGoalDoneAndSendToTasksCreatesTaskOnce() = runTest {
        withChild()
        val goal = Fixtures.goal("수학", id = "g")
        val s1 = Fixtures.step("g", "g3s1", "a", id = "s1", status = MilestoneStatus.DONE)
        val s2 = Fixtures.step("g", "g3s2", "b", id = "s2", order = 1)
        val sent = Fixtures.step("g", "g4s1", "c", id = "s3", order = 2, taskId = "t-old")
        streams.goals.value = listOf(goal); streams.goalSteps.value = listOf(s1, s2, sent)
        val vm = vm(); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(GoalsEvent.SendStepToTasks(s2, "PARENT")); vm.onEvent(GoalsEvent.SendStepToTasks(sent, "PARENT")); settle(vm.state)
        val task = tasks.saved.single()
        assertEquals("b", task.title); assertEquals(LocalDate.of(2030, 2, 28).toEpochDay(), task.dueDate); assertEquals("수학", task.note)
        assertEquals(listOf("stepTask:s2:set"), goals.calls)
        goals.calls.clear()
        vm.onEvent(GoalsEvent.SetStepStatus(s2, MilestoneStatus.DONE)); settle(vm.state)
        assertEquals(listOf("stepStatus:s2:DONE"), goals.calls)
        goals.calls.clear()
        vm.onEvent(GoalsEvent.SetStepStatus(sent, MilestoneStatus.DONE)); settle(vm.state)
        assertEquals(listOf("stepStatus:s3:DONE"), goals.calls) // s2 는 스트림상 아직 미완료라 목표 완료 아님
        streams.goalSteps.value = listOf(s1, s2.copy(status = MilestoneStatus.DONE), sent)
        settle(vm.state); goals.calls.clear()
        vm.onEvent(GoalsEvent.SetStepStatus(sent, MilestoneStatus.DONE)); settle(vm.state)
        assertEquals(listOf("stepStatus:s3:DONE", "goalStatus:g:DONE"), goals.calls)
        vm.onEvent(GoalsEvent.SetGoalStatus("g", GoalStatus.ARCHIVED)); vm.onEvent(GoalsEvent.DeleteGoal("g")); settle(vm.state)
        assertEquals(listOf("goalStatus:g:ARCHIVED", "delete:g"), goals.calls.takeLast(2))
        job.cancel()
    }
}
