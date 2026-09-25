package com.nextstep.app.ui.yearplan

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.growth.YearProfiles
import com.nextstep.app.domain.year.YearArea
import com.nextstep.app.domain.year.YearPlans
import com.nextstep.app.domain.year.YearTerm
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeJourneyRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        assertEquals(plan.size, s.total)
        assertEquals(listOf("전체") + YearPlans.areasOf(yearKey).map { it.label }, s.tabs.map { it.label })
        assertNull(s.tabs.first().area)
        val order = s.tabs.first().sections.map { it.first }
        assertEquals(listOf(YearTerm.SECOND, YearTerm.ALL_YEAR, YearTerm.FIRST).filter { t -> plan.any { it.term == t } }, order)
        s.tabs.drop(1).forEach { tab -> assertTrue(tab.sections.flatMap { it.second }.all { it.task.area == tab.area }) }
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
}
