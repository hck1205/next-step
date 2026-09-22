package com.nextstep.app.ui.calendar

import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeEventRepository
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class CalendarViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams()
    private val events = FakeEventRepository(); private val tasks = FakeTaskRepository()
    private val today = DateUtils.today()

    private fun vm() = CalendarViewModel(streams, events, tasks)

    @Test
    fun markersAndSelectedDayReflectData() = runTest {
        val exam = Fixtures.event("시험", today, LocalTime.of(9, 0), LocalTime.of(10, 0), EventType.EXAM)
        streams.events.value = listOf(exam)
        streams.tasks.value = listOf(Fixtures.task("할 일", today), Fixtures.task("끝", today, done = true))
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(20, 0), 25))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        val marker = s.markers.getValue(today)
        assertTrue(marker.hasExam); assertTrue(marker.hasTask); assertEquals(25, marker.studyMinutes)
        assertEquals(1, s.dayEvents.size); assertEquals(2, s.dayTasks.size); assertEquals(25, s.dayMinutes)
        job.cancel()
    }

    @Test
    fun monthNavigationAndSelectionKeepMonthInSync() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        vm.onEvent(CalendarEvent.NextMonth)
        assertEquals(YearMonth.now().plusMonths(1), settle(vm.state).month)
        vm.onEvent(CalendarEvent.PrevMonth); vm.onEvent(CalendarEvent.PrevMonth)
        assertEquals(YearMonth.now().minusMonths(1), settle(vm.state).month)
        val far = LocalDate.of(2030, 1, 15)
        vm.onEvent(CalendarEvent.Select(far))
        val s = settle(vm.state)
        assertEquals(far, s.selected); assertEquals(YearMonth.of(2030, 1), s.month)
        vm.onEvent(CalendarEvent.Today)
        assertEquals(LocalDate.now(), settle(vm.state).selected)
        job.cancel()
    }

    @Test
    fun saveEventNormalizesEndBeforeStartAndKeepsIdOnEdit() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        val date = LocalDate.of(2026, 9, 1)
        vm.onEvent(CalendarEvent.SaveEvent(null, "학원", "math", EventType.ACADEMY, date, LocalTime.of(18, 0), LocalTime.of(17, 0), true, "역삼", ""))
        settle(vm.state)
        val saved = events.saved.single()
        assertEquals(DateUtils.toMillis(date, LocalTime.of(19, 0)), saved.endAt) // 종료가 시작보다 이르면 +1시간
        assertTrue(saved.repeatWeekly); assertEquals("역삼", saved.location)
        vm.onEvent(CalendarEvent.SaveEvent(saved, "학원2", null, EventType.OTHER, date, LocalTime.of(9, 0), LocalTime.of(10, 0), false, "", ""))
        settle(vm.state)
        assertEquals(saved.id, events.saved[1].id); assertEquals("학원2", events.saved[1].title); assertFalse(events.saved[1].repeatWeekly)
        vm.onEvent(CalendarEvent.DeleteEvent(saved.id))
        settle(vm.state)
        assertEquals(listOf(saved.id), events.deleted)
        job.cancel()
    }

    @Test
    fun taskEventsCreateEditToggleAndDelete() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        vm.onEvent(CalendarEvent.SaveTask(null, "숙제", "math", TaskType.HOMEWORK, today, "MENTOR"))
        settle(vm.state)
        val created = tasks.saved.single()
        assertEquals("MENTOR", created.createdByRole); assertEquals(today.toEpochDay(), created.dueDate)
        vm.onEvent(CalendarEvent.SaveTask(created, "숙제!", null, TaskType.REVIEW, today.plusDays(1), "STUDENT"))
        vm.onEvent(CalendarEvent.ToggleTask(created))
        vm.onEvent(CalendarEvent.DeleteTask(created.id))
        settle(vm.state)
        assertEquals(created.id, tasks.saved[1].id); assertEquals("MENTOR", tasks.saved[1].createdByRole) // 작성자는 유지
        assertTrue(tasks.saved[2].done)
        assertTrue(tasks.saved.last().deleted)
        job.cancel()
    }
}
