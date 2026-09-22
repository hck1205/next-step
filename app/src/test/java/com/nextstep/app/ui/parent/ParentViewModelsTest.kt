package com.nextstep.app.ui.parent

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeNoteRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class ParentViewModelsTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val tasks = FakeTaskRepository(); private val notes = FakeNoteRepository()
    private val today = DateUtils.today()

    @Test
    fun dashboardAggregatesFamilyCounts() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "나"), Fixtures.member(Role.PARENT, "엄마"), Fixtures.member(Role.PARENT, "아빠", mentorEnabled = true), Fixtures.member(Role.MENTOR, "쌤"))
        streams.roadmap.value = listOf(Fixtures.roadmap("a", status = RoadmapStatus.DONE), Fixtures.roadmap("b"))
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 40), Fixtures.session("math", today.minusDays(1), LocalTime.of(9, 0), 40))
        streams.tasks.value = listOf(Fixtures.task("지남", today.minusDays(1)))
        val vm = ParentDashboardViewModel(streams, tasks, notes); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(2, s.parentCount); assertEquals(2, s.mentorCount); assertEquals(1, s.roadmapDone); assertEquals(2, s.roadmapTotal)
        assertEquals(2, s.streak); assertEquals(40, s.todayMinutes); assertEquals(1, s.overdueCount)
        assertEquals(7, s.daily.size)
        job.cancel()
    }

    @Test
    fun dashboardEventsWriteNotesAndTasks() = runTest {
        val vm = ParentDashboardViewModel(streams, tasks, notes); val job = subscribe(vm.state)
        vm.onEvent(ParentDashboardEvent.AddNote("  ")); vm.onEvent(ParentDashboardEvent.AddNote("잘했어"))
        vm.onEvent(ParentDashboardEvent.DeleteNote("n"))
        vm.onEvent(ParentDashboardEvent.AssignTask("영단어", "eng", TaskType.HOMEWORK, today, "MENTOR"))
        settle(vm.state)
        assertEquals(listOf("잘했어"), notes.added); assertEquals(listOf("n"), notes.deleted)
        assertEquals("MENTOR", tasks.saved.single().createdByRole)
        job.cancel()
    }

    @Test
    fun cheerSuggestionsReflectTodayAndAlwaysHaveFallback() = runTest {
        val vm = CheerViewModel(streams, notes); val job = subscribe(vm.state)
        val empty = settle(vm.state)
        assertTrue(empty.cheerSuggestions.first().contains("수고했어"))
        assertEquals(2, empty.cheerSuggestions.size)

        streams.subjects.value = listOf(Fixtures.math)
        streams.sessions.value = (0..3).map { Fixtures.session("math", today.minusDays(it.toLong()), LocalTime.of(20, 0), 70) }
        streams.tasks.value = listOf(Fixtures.task("끝", today, done = true).copy(updatedAt = System.currentTimeMillis()))
        val s = settle(vm.state)
        assertEquals(4, s.streak); assertEquals(70, s.todayMinutes); assertEquals(1, s.todayDoneTasks)
        assertTrue(s.cheerSuggestions.any { it.contains("1시간 10분") })
        assertTrue(s.cheerSuggestions.any { it.contains("4일 연속") })
        assertTrue(s.cheerSuggestions.any { it.contains("수학") })
        vm.onEvent(CheerEvent.Send(s.cheerSuggestions.first())); vm.onEvent(CheerEvent.Delete("x"))
        settle(vm.state)
        assertEquals(1, notes.added.size); assertEquals(listOf("x"), notes.deleted)
        job.cancel()
    }
}
