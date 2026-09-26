package com.nextstep.app.ui.home

import com.nextstep.app.fake.FakeMemberRepository
import com.nextstep.app.domain.growth.StudentHomeSection
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.planner.PlanOptions
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeContentRepository
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeProjectRepository
import com.nextstep.app.fake.FakeWeekPlanRepository
import com.nextstep.app.domain.selfdirection.SelfDirection
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.domain.project.ProjectCatalog
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.fake.FakeRoadmapRepository
import com.nextstep.app.fake.FakeStudyPlanRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.fake.FakeTopicRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class HomeViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams()
    private val tasks = FakeTaskRepository(); private val topics = FakeTopicRepository()
    private val roadmap = FakeRoadmapRepository(); private val contents = FakeContentRepository(); private val plans = FakeStudyPlanRepository()
    private val members = FakeMemberRepository()
    private val projects = FakeProjectRepository(streams)
    private val weekPlans = FakeWeekPlanRepository(streams)
    private val today = DateUtils.today()

    private fun vm() = HomeViewModel(streams, tasks, topics, roadmap, contents, plans, members, projects, weekPlans)

    @Test
    fun youngStudentGetsSproutScreenWeekStarsAndSeenLevelIsRecorded() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "하은", id = "kid", gradeYear = 1))
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(0, 0), 20))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(StudentUiLevel.SPROUT, s.level); assertNull(s.levelUp); assertEquals("kid", s.studentId)
        assertEquals(7, s.week.size); assertEquals(20, s.week.last().minutes); assertEquals(1, s.streak)
        assertEquals(listOf("seen:kid:SPROUT"), members.calls)
        job.cancel()
    }

    @Test
    fun levelUpCardListsNewCardsUntilDismissedAndNeverShowsWhenLowered() = runTest {
        val kid = Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 3)
        streams.members.value = listOf(kid.copy(seenUiLevel = "SPROUT"))
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals(StudentUiLevel.SEEDLING, s.levelUp)
        assertEquals(listOf(StudentHomeSection.EVENTS, StudentHomeSection.REVIEW, StudentHomeSection.RECOMMENDATION), s.newSections)
        vm.onEvent(HomeEvent.DismissLevelUp); settle(vm.state)
        assertEquals(listOf("seen:kid:SEEDLING"), members.calls)
        streams.members.value = listOf(kid.copy(uiLevel = "SPROUT", seenUiLevel = "SEEDLING"))
        s = settle(vm.state)
        assertEquals(StudentUiLevel.SPROUT, s.level); assertNull(s.levelUp); assertTrue(s.newSections.isEmpty())
        job.cancel()
    }

    @Test
    fun eachYearBringsItsOwnStudyKindsOrderAndAmount() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 3))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals("e3", s.year!!.key); assertEquals(40, s.year!!.dailyMinutes); assertEquals(3, s.taskRows)
        assertEquals(listOf(StudentHomeSection.TIMER, StudentHomeSection.YEAR), s.homeOrder.take(2))
        assertEquals(20, s.planDefaults.sessionMinutes)
        vm.onEvent(HomeEvent.AddStudyKind(s.year!!.kinds.first())); settle(vm.state)
        assertEquals("영어 듣기·단어 10분", tasks.saved.single().title); assertEquals(today.toEpochDay(), tasks.saved.single().dueDate)
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 8).copy(seenUiLevel = "BRANCH"))
        val m2 = settle(vm.state)
        assertEquals("m2", m2.year!!.key); assertEquals(listOf(StudentHomeSection.TIMER, StudentHomeSection.MISSION), m2.homeOrder.take(2))
        job.cancel()
    }

    @Test
    fun withoutStudentInfoTheFullScreenIsUsed() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        assertEquals(StudentUiLevel.TREE, settle(vm.state).level); assertTrue(members.calls.isEmpty())
        job.cancel()
    }

    @Test
    fun missionFocusShowsNextStepOfDatedGoals() = runTest {
        val goal = Fixtures.goal("국어 단원평가", id = "m").copy(targetDate = today.plusDays(4).toEpochDay())
        streams.goals.value = listOf(goal, Fixtures.goal("피아노", id = "p"))
        streams.goalSteps.value = listOf(
            Fixtures.step("m", "g5s1", "범위 확인", id = "a", status = com.nextstep.app.data.model.MilestoneStatus.DONE),
            Fixtures.step("m", "g5s1", "문제 풀기", id = "b", order = 1).copy(dueDate = today.plusDays(2).toEpochDay()),
        )
        val vm = vm(); val job = subscribe(vm.state)
        val f = settle(vm.state).missionFocus.single()
        assertEquals("문제 풀기", f.nextStep.title); assertEquals(4, f.daysLeft); assertEquals(1, f.doneCount)
        job.cancel()
    }

    @Test
    fun stateDerivesTodayFiguresQueuesAndNextExam() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.topics.value = Fixtures.topics("math", 4, covered = 2, reviewed = 0)
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(8, 0), 30))
        streams.tasks.value = listOf(Fixtures.task("오늘", today), Fixtures.task("내일", today.plusDays(1)))
        streams.events.value = listOf(Fixtures.event("기말", today.plusDays(3), LocalTime.of(9, 0), LocalTime.of(10, 0), EventType.EXAM))
        streams.roadmap.value = listOf(Fixtures.roadmap("끝", status = RoadmapStatus.DONE), Fixtures.roadmap("진행", status = RoadmapStatus.IN_PROGRESS), Fixtures.roadmap("예정"))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(30, s.todayMinutes); assertEquals(180, s.weekGoalMinutes)
        assertEquals(listOf("오늘"), s.pendingTasks.map { it.title })
        assertEquals("기말", s.nextExam!!.title)
        assertEquals(2, s.reviewQueue.size); assertEquals("단원 2", s.previewQueue.single().second.title)
        assertEquals(listOf("진행", "예정"), s.roadmapFocus.map { it.title })
        assertEquals(1, s.activeSubjects.size)
        assertEquals(com.nextstep.app.domain.planner.PlanOptions(), s.planDefaults)
        streams.members.value = listOf(Fixtures.member(com.nextstep.app.data.model.Role.STUDENT, "나", gradeYear = 11))
        val high = settle(vm.state)
        assertEquals(com.nextstep.app.domain.growth.GrowthStage.HIGH, high.stage); assertEquals(60, high.planDefaults.sessionMinutes)
        job.cancel()
    }

    @Test
    fun eventsDelegateToRepositories() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        val task = Fixtures.task("숙제", today)
        val topic = Fixtures.topic("math", "단원", 0)
        vm.onEvent(HomeEvent.ToggleTask(task))
        vm.onEvent(HomeEvent.MarkTopic(topic, TopicStatus.REVIEWED))
        vm.onEvent(HomeEvent.AddQuickTask(Fixtures.math, topic, TaskType.PREVIEW))
        vm.onEvent(HomeEvent.SetRoadmapStatus("r", RoadmapStatus.DONE))
        vm.onEvent(HomeEvent.MarkContentWatched("c"))
        settle(vm.state)
        assertEquals(listOf("status:t-math-0:REVIEWED"), topics.calls)
        assertEquals("수학 단원 예습", tasks.saved.single().title)
        assertEquals(listOf("r" to RoadmapStatus.DONE), roadmap.statuses)
        assertEquals(listOf("c" to true), contents.watched)
        job.cancel()
    }

    @Test
    fun generatePlanAppliesPlanAndExposesResultUntilDismissed() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.topics.value = Fixtures.topics("math", 3, covered = 2)
        val vm = vm(); val job = subscribe(vm.state)
        settle(vm.state)
        vm.onEvent(HomeEvent.GeneratePlan(PlanOptions(days = 2, sessionsPerDay = 1)))
        val s = settle(vm.state)
        assertEquals(1, plans.applied.size)
        assertEquals(2, s.lastPlan!!.events.size)
        vm.onEvent(HomeEvent.DismissPlanResult)
        assertNull(settle(vm.state).lastPlan)
        job.cancel()
    }

    @Test
    fun emptyQueueProducesEmptyPlanWithoutRepositoryCall() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        settle(vm.state)
        vm.onEvent(HomeEvent.GeneratePlan(PlanOptions()))
        val s = settle(vm.state)
        assertTrue(s.lastPlan!!.isEmpty)
        assertEquals(1, plans.applied.size) // 저장소가 빈 계획을 무시하는 책임을 가짐
        job.cancel()
    }

    @Test
    fun todaysRoutineComesFromRunningProjectsAndTogglesOnTap() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "하은", id = "kid", gradeYear = 1))
        val (goal, steps) = ProjectPlanner.start(ProjectCatalog.byId.getValue("english-reader"), 4, today, "PARENT")
        streams.goals.value = listOf(goal.copy(familyId = Fixtures.FAMILY)); streams.goalSteps.value = steps
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertTrue(StudentHomeSection.ROUTINE in s.homeOrder)
        val p = s.routines.single(); val item = p.current!!.routine.first()
        assertEquals("파닉스", p.current!!.title)
        vm.onEvent(HomeEvent.ToggleRoutine(p, item)); s = settle(vm.state)
        assertEquals(setOf(item.name), s.routines.single().todayDoneItems); assertEquals(item.minutes, s.routines.single().todayMinutes)
        vm.onEvent(HomeEvent.ToggleRoutine(s.routines.single(), item)); s = settle(vm.state)
        assertTrue(s.routines.single().todayDoneItems.isEmpty())
        job.cancel()
    }

    @Test
    fun myWeekLetsAFifthGraderPlanAndReflectButNotApprove() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 5))
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertTrue(StudentHomeSection.MY_WEEK in s.homeOrder)
        assertEquals(SelfDirectionStage.PLAN_FIRST, s.myWeek!!.stage)
        assertTrue(s.myWeekAccess.canPlan); assertTrue(s.myWeekAccess.canReflect); assertTrue(s.myWeekAccess.forChild); assertFalse(s.myWeekAccess.canApprove)
        vm.onEvent(HomeEvent.SaveWeekPlan(listOf("영어 책 3권"), 180)); s = settle(vm.state)
        assertEquals(listOf("영어 책 3권"), s.myWeek!!.plan!!.goalList); assertTrue(s.myWeek!!.waitingApproval)
        vm.onEvent(HomeEvent.ToggleWeekGoal(s.myWeek!!.plan!!.id, 0)); s = settle(vm.state)
        assertEquals(1, s.myWeek!!.plan!!.doneCount)
        vm.onEvent(HomeEvent.ReflectWeek(SelfDirection.weekStart(today), 3, "매일 읽음", "", "아침에")); settle(vm.state)
        assertEquals("reflect:${SelfDirection.weekStart(today)}:3:매일 읽음::아침에", weekPlans.calls.last())
        job.cancel()
    }

    @Test
    fun youngChildFollowsTheAdultsPlan() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "하은", id = "kid", gradeYear = 1))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(SelfDirectionStage.CHOOSE, s.myWeek!!.stage)
        assertFalse(s.myWeekAccess.canPlan); assertTrue(s.myWeekAccess.canCheck); assertTrue(s.myWeekAccess.canReflect)
        job.cancel()
    }
}
