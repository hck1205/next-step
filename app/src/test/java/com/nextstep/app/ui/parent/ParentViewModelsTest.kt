package com.nextstep.app.ui.parent

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeProjectRepository
import com.nextstep.app.fake.FakeWeekPlanRepository
import com.nextstep.app.domain.selfdirection.SelfDirection
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
    private val today = DateUtils.today()

    @Test
    fun dashboardShowsOnlyWhatTheSimpleHomeNeeds() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "나", gradeYear = 8), Fixtures.member(Role.PARENT, "엄마"), Fixtures.member(Role.PARENT, "아빠", mentorEnabled = true), Fixtures.member(Role.MENTOR, "쌤"))
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 40), Fixtures.session("math", today.minusDays(1), LocalTime.of(9, 0), 40))
        streams.tasks.value = listOf(Fixtures.task("지남", today.minusDays(1)))
        val vm = ParentDashboardViewModel(streams, tasks, projects, weekPlans); val job = subscribe(vm.state)
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
    fun dashboardAssignsTasks() = runTest {
        val vm = ParentDashboardViewModel(streams, tasks, projects, weekPlans); val job = subscribe(vm.state)
        vm.onEvent(ParentDashboardEvent.AssignTask("영단어", "eng", TaskType.HOMEWORK, today, "MENTOR"))
        settle(vm.state)
        assertEquals("MENTOR", tasks.saved.single().createdByRole)
        job.cancel()
    }

    @Test
    fun dashboardShowsTodaysRoutineButNotAsAMission() = runTest {
        val (goal, steps) = ProjectPlanner.start(ProjectCatalog.byId.getValue("korean-reader"), 0, today, "PARENT")
        streams.goals.value = listOf(goal.copy(familyId = Fixtures.FAMILY)); streams.goalSteps.value = steps
        val vm = ParentDashboardViewModel(streams, tasks, projects, weekPlans); val job = subscribe(vm.state)
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
        val vm = ParentDashboardViewModel(streams, tasks, projects, weekPlans); val job = subscribe(vm.state)
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
        weekPlans.savePlan(SelfDirection.weekStart(today), listOf("수학 익힘 3쪽"), 180)
        s = settle(vm.state)
        assertTrue(s.week!!.waitingApproval)
        vm.onEvent(ParentDashboardEvent.ApproveWeek(s.week!!.plan!!.id))
        assertFalse(settle(vm.state).week!!.waitingApproval)
        job.cancel()
    }
}
