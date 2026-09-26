package com.nextstep.app.ui.parent

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeProjectRepository
import com.nextstep.app.fake.FakeWeekPlanRepository
import com.nextstep.app.fake.FakeRewardRepository
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.domain.project.ProjectCatalog
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class ParentViewModelsTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val tasks = FakeTaskRepository()
    private val projects = FakeProjectRepository(streams)
    private val weekPlans = FakeWeekPlanRepository(streams)
    private val rewards = FakeRewardRepository(streams)
    private val today = DateUtils.today()

    @Test
    fun dashboardShowsOnlyWhatTheSimpleHomeNeeds() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "나", gradeYear = 8), Fixtures.member(Role.PARENT, "엄마"), Fixtures.member(Role.PARENT, "아빠", mentorEnabled = true), Fixtures.member(Role.MENTOR, "쌤"))
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 40), Fixtures.session("math", today.minusDays(1), LocalTime.of(9, 0), 40))
        streams.tasks.value = listOf(Fixtures.task("지남", today.minusDays(1)))
        val vm = ParentDashboardViewModel(streams, tasks, projects, weekPlans, rewards); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(2, s.streak); assertEquals(1, s.overdueCount); assertEquals(listOf("지남"), s.pendingTasks.map { it.title })
        assertEquals(com.nextstep.app.domain.growth.GrowthStage.MIDDLE, s.stage)
        // 상태 문장: 균형 판단 + 챙길 것(기한 지난 할 일 1개)
        assertTrue(s.balance != null); assertTrue(s.statusHeadline.contains("챙길 것 하나")); assertTrue(s.statusContext.startsWith("이번 주"))
        assertTrue(s.missionFocus.isEmpty())
        assertEquals("fam", s.activeFamilyId)
        streams.profile.value = streams.profile.value.copy(children = listOf(com.nextstep.app.data.prefs.LinkedChild("fam", "지우", "A", "me"), com.nextstep.app.data.prefs.LinkedChild("famB", "하은", "B", "me2")))
        assertEquals(listOf("지우", "하은"), settle(vm.state).children.map { it.studentName })
        streams.goals.value = listOf(Fixtures.goal("중간고사", id = "e").copy(targetDate = today.plusDays(20).toEpochDay()))
        streams.goalSteps.value = listOf(Fixtures.step("e", "g8s1", "범위 확인", id = "s"))
        assertEquals("범위 확인", settle(vm.state).missionFocus.single().nextStep.title)
        job.cancel()
    }

    @Test
    fun dashboardListsRewardsThatAreDueAndGivesThem() = runTest {
        streams.goals.value = listOf(Fixtures.goal("분수", trackId = GoalTree.TRACK, id = "g1", status = com.nextstep.app.data.model.GoalStatus.DONE), Fixtures.goal("일기", trackId = GoalTree.TRACK, id = "g2"))
        streams.rewards.value = listOf(
            com.nextstep.app.data.local.entity.RewardEntity(id = "r1", familyId = Fixtures.FAMILY, kind = "GOAL", targetId = "g1", title = "보드게임"),
            com.nextstep.app.data.local.entity.RewardEntity(id = "r2", familyId = Fixtures.FAMILY, kind = "GOAL", targetId = "g2", title = "나들이"),
            com.nextstep.app.data.local.entity.RewardEntity(id = "r3", familyId = Fixtures.FAMILY, kind = "LEVEL", targetId = "2", title = "영화"), // 목표 달성 10 + 할 일 5개 15 = 25 XP → 레벨 2
        )
        streams.tasks.value = (1..5).map { Fixtures.task("t$it", today, done = true, by = "PARENT").copy(doneAt = System.currentTimeMillis()) }
        val vm = ParentDashboardViewModel(streams, tasks, projects, weekPlans, rewards); val job = subscribe(vm.state)
        assertEquals(setOf("r1", "r3"), settle(vm.state).rewardsDue.map { it.reward.id }.toSet())
        vm.onEvent(ParentDashboardEvent.GiveReward("r1"))
        assertEquals(listOf("r3"), settle(vm.state).rewardsDue.map { it.reward.id })
        assertEquals(listOf("give:r1"), rewards.calls)
        job.cancel()
    }

    @Test
    fun dashboardAssignsTasks() = runTest {
        val vm = ParentDashboardViewModel(streams, tasks, projects, weekPlans, rewards); val job = subscribe(vm.state)
        vm.onEvent(ParentDashboardEvent.AssignTask("영단어", "eng", TaskType.HOMEWORK, today, "MENTOR"))
        settle(vm.state)
        assertEquals("MENTOR", tasks.saved.single().createdByRole)
        job.cancel()
    }

    @Test
    fun dashboardShowsTodaysRoutineButNotAsAMission() = runTest {
        val (goal, steps) = ProjectPlanner.start(ProjectCatalog.byId.getValue("korean-reader"), 0, today, "PARENT")
        streams.goals.value = listOf(goal.copy(familyId = Fixtures.FAMILY)); streams.goalSteps.value = steps
        val vm = ParentDashboardViewModel(streams, tasks, projects, weekPlans, rewards); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.missionFocus.isEmpty())
        val p = s.routines.single()
        vm.onEvent(ParentDashboardEvent.ToggleRoutine(p, p.current!!.routine.single()))
        assertEquals(10, settle(vm.state).routines.single().todayMinutes)
        assertEquals(1, projects.calls.size)
        job.cancel()
    }

    @Test
    fun parentPlansForYoungChildAndOnlyConfirmsAnOlderChildsPlan() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "하은", id = "kid", gradeYear = 1))
        val vm = ParentDashboardViewModel(streams, tasks, projects, weekPlans, rewards); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals(SelfDirectionStage.CHOOSE, s.week!!.stage); assertTrue(s.weekAccess.canPlan); assertFalse(s.weekAccess.canCheck)
        weekPlans.role = "PARENT"
        vm.onEvent(ParentDashboardEvent.SaveWeekPlan(listOf("그림책 3권", "줄넘기"), 0)); s = settle(vm.state)
        assertEquals(2, s.week!!.plan!!.goalList.size)
        streams.weekPlans.value = emptyList()
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 5))
        s = settle(vm.state)
        assertEquals(SelfDirectionStage.PLAN_FIRST, s.week!!.stage); assertFalse(s.weekAccess.canPlan); assertTrue(s.weekAccess.canApprove)
        weekPlans.role = "STUDENT"
        weekPlans.savePlan(DateUtils.weekStart(today), listOf("수학 익힘 3쪽"), 180)
        s = settle(vm.state)
        assertTrue(s.week!!.waitingApproval)
        vm.onEvent(ParentDashboardEvent.ApproveWeek(s.week!!.plan!!.id))
        assertFalse(settle(vm.state).week!!.waitingApproval)
        job.cancel()
    }

    @Test
    fun dashboardMonitorsParentGivenGoalsWithOverdueFirst() = runTest {
        streams.goals.value = listOf(
            Fixtures.goal("영어 일기", trackId = GoalTree.TRACK, id = "a"), Fixtures.goal("줄넘기", trackId = GoalTree.TRACK, id = "b"),
            Fixtures.goal("끝난 것", trackId = GoalTree.TRACK, id = "c", status = com.nextstep.app.data.model.GoalStatus.DONE),
        )
        streams.tasks.value = listOf(Fixtures.task("3줄 쓰기", today.minusDays(1), by = "PARENT").copy(goalId = "b"))
        val vm = ParentDashboardViewModel(streams, tasks, projects, weekPlans, rewards); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(listOf("b", "a"), s.goalFocus.map { it.goal.id })
        assertTrue(s.missionFocus.isEmpty())
        job.cancel()
    }
}
