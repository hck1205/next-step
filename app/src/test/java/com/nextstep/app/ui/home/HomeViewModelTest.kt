package com.nextstep.app.ui.home

import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.planner.PlanOptions
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeContentRepository
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeRoadmapRepository
import com.nextstep.app.fake.FakeStudyPlanRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.fake.FakeTopicRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class HomeViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams()
    private val tasks = FakeTaskRepository(); private val topics = FakeTopicRepository()
    private val roadmap = FakeRoadmapRepository(); private val contents = FakeContentRepository(); private val plans = FakeStudyPlanRepository()
    private val today = DateUtils.today()

    private fun vm() = HomeViewModel(streams, tasks, topics, roadmap, contents, plans)

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
}
