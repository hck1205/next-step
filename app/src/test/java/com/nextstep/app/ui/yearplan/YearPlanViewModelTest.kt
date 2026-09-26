package com.nextstep.app.ui.yearplan

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.growth.YearProfiles
import com.nextstep.app.domain.year.YearArea
import com.nextstep.app.domain.year.YearPlans
import com.nextstep.app.domain.year.AheadPlans
import com.nextstep.app.domain.year.YearTerm
import com.nextstep.app.domain.year.YearTrends
import com.nextstep.app.domain.year.YearDoer
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeJourneyRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class YearPlanViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.STUDENT)
    private val journey = FakeJourneyRepository()
    private val tasks = FakeTaskRepository()
    private val today = LocalDate.of(2029, 10, 10)
    private val student = Fixtures.member(Role.STUDENT, "학생", birthDate = LocalDate.of(2020, 5, 1))
    private val yearKey = YearProfiles.of(student, today)!!.key

    private fun vm() = YearPlanViewModel(streams, journey, tasks, today = { today })

    @Test
    fun tabsStartWithAllThenAreasAndSecondTermComesFirstInAutumn() = runTest {
        streams.members.value = listOf(student)
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        val plan = YearPlans.forYear(yearKey)
        assertEquals(yearKey, s.year!!.key)
        assertEquals(YearTerm.SECOND, s.currentTerm)
        assertEquals(YearPlans.base(yearKey).size, s.total) // 진행률은 기본만
        assertEquals(YearPlans.ahead(yearKey).size, s.aheadTotal)
        assertEquals(AheadPlans.heading(yearKey), s.aheadHeading)
        assertEquals(YearTrends.of(yearKey), s.trend)
        assertFalse(s.showsAllDoers) // 학교부터는 "스스로"가 아닌 줄에만 칩
        assertEquals(listOf("전체") + YearPlans.areasOf(yearKey).map { it.label }, s.tabs.map { it.label })
        assertNull(s.tabs.first().area)
        val order = s.tabs.first().sections.map { it.first }
        assertEquals(listOf(YearTerm.SECOND, YearTerm.ALL_YEAR, YearTerm.FIRST).filter { t -> plan.any { it.term == t } }, order)
        s.tabs.drop(1).forEach { tab -> assertTrue(tab.sections.flatMap { it.second }.all { it.task.area == tab.area }) }
        // 기본 묶음에는 앞서 가기가 없고, 앞서 가기는 탭마다 따로 맨 아래
        assertTrue(s.tabs.first().sections.flatMap { it.second }.none { it.task.isAhead })
        assertEquals(YearPlans.ahead(yearKey).toSet(), s.tabs.first().ahead.map { it.task }.toSet())
        s.tabs.drop(1).forEach { tab -> assertTrue(tab.ahead.all { it.task.area == tab.area && it.task.isAhead }) }
        job.cancel()
    }

    @Test
    fun doneComesFromStoredYearItemsOnly() = runTest {
        val first = YearPlans.forYear(yearKey).first()
        streams.members.value = listOf(student)
        streams.journeyItems.value = listOf(
            Fixtures.journeyItem(first.storageId(yearKey), status = MilestoneStatus.DONE),
            Fixtures.journeyItem("year:other:MATH:x", status = MilestoneStatus.DONE),
            Fixtures.journeyItem("CATALOG_ITEM", status = MilestoneStatus.DONE),
        )
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(1, s.done)
        assertEquals(1, s.tabs.first().done)
        assertTrue(s.tabs.first().sections.flatMap { it.second }.single { it.task == first }.done)
        job.cancel()
    }

    @Test
    fun toggleStoresYearKeyWithTermEndAndAddToTodaySavesTask() = runTest {
        streams.members.value = listOf(student)
        val vm = vm(); val job = subscribe(vm.state)
        val view = settle(vm.state).tabs.first().sections.first().second.first()
        vm.onEvent(YearPlanEvent.Toggle(view))
        vm.onEvent(YearPlanEvent.AddToToday(view.task))
        settle(vm.state)
        assertEquals(listOf("tStatus:${view.task.storageId(yearKey)}:DONE:${view.task.term.endDate(today)}"), journey.calls)
        val saved = tasks.saved.single()
        assertEquals(view.task.title, saved.title)
        assertEquals(view.task.how, saved.note)
        assertEquals(today.toEpochDay(), saved.dueDate)
        assertEquals(if (view.task.area == YearArea.EXAM) TaskType.EXAM_PREP else TaskType.HOMEWORK, saved.type)
        job.cancel()
    }

    @Test
    fun withoutStudentAgeThereAreNoTabs() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "학생"))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.loaded)
        assertNull(s.year)
        assertTrue(s.tabs.isEmpty())
        job.cancel()
    }

    @Test
    fun infantYearShowsWhoDoesEachTask() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아기", birthDate = today.minusMonths(5)))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals("a0", s.year!!.key)
        assertTrue(s.showsAllDoers)
        assertTrue(s.trend!!.advice.contains("0분"))
        job.cancel()
    }

    @Test
    fun mineFilterShowsOnlyTheViewersShareAndFallsBackToAll() = runTest {
        streams.members.value = listOf(student)
        val vm = vm(); val job = subscribe(vm.state)
        val all = settle(vm.state)
        assertFalse(all.mineOnly)
        vm.onEvent(YearPlanEvent.SetMine(setOf(YearDoer.MENTOR)))
        val mentor = settle(vm.state)
        assertTrue(mentor.mineOnly)
        assertEquals(YearPlans.base(yearKey).count { it.who == YearDoer.MENTOR }, mentor.total)
        assertEquals(YearPlans.base(yearKey).size, mentor.allCount)
        vm.onEvent(YearPlanEvent.ShowMine(false))
        assertEquals(mentor.allCount, settle(vm.state).total)
        // 내 몫이 하나도 없으면 전체를 보여 줘요.
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아기", birthDate = today.minusMonths(5)))
        vm.onEvent(YearPlanEvent.ShowMine(true))
        val infant = settle(vm.state)
        assertFalse(infant.mineOnly)
        assertEquals(infant.allCount, infant.total)
        job.cancel()
    }

    @Test
    fun doneAheadTasksCountSeparatelyAndUndoneComeFirst() = runTest {
        val ahead = YearPlans.ahead(yearKey).first()
        val base = YearPlans.base(yearKey).first()
        streams.members.value = listOf(student)
        streams.journeyItems.value = listOf(
            Fixtures.journeyItem(ahead.storageId(yearKey), status = MilestoneStatus.DONE),
            Fixtures.journeyItem(base.storageId(yearKey), status = MilestoneStatus.DONE),
        )
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(1, s.done); assertEquals(1, s.aheadDone)
        assertEquals(1, s.tabs.first().done) // 탭의 끝낸 수도 기본만
        assertTrue(s.tabs.first().ahead.last().done) // 끝낸 것은 아래로
        s.tabs.first().sections.forEach { (_, views) -> assertEquals(views.sortedBy { it.done }, views) }
        job.cancel()
    }

    @Test
    fun schoolYearShowsAheadAsAheadAndPreschoolAsPlay() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", gradeYear = 5))
        val vm = vm(); val job = subscribe(vm.state)
        val school = settle(vm.state)
        assertEquals("e5", school.year!!.key)
        assertEquals("앞서 가기", school.aheadHeading); assertTrue(school.aheadNote.contains("여유"))
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "하은", birthDate = today.minusYears(4).minusMonths(2)))
        val preschool = settle(vm.state)
        assertTrue(preschool.year!!.key.startsWith("a"))
        assertEquals("더 해 보면 좋은 것", preschool.aheadHeading); assertTrue(preschool.aheadNote.contains("놀이"))
        job.cancel()
    }
}
