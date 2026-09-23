package com.nextstep.app.ui.quickadd

import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.fake.FakeActivityRepository
import com.nextstep.app.fake.FakeEventRepository
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGradeRepository
import com.nextstep.app.fake.FakeNoteRepository
import com.nextstep.app.fake.FakeTaskRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class QuickAddViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val notes = FakeNoteRepository(); private val activities = FakeActivityRepository(); private val tasks = FakeTaskRepository()
    private val grades = FakeGradeRepository(); private val events = FakeEventRepository()
    private val today = LocalDate.of(2029, 10, 10)

    private fun vm() = QuickAddViewModel(streams, notes, activities, tasks, grades, events, today = { today })

    @Test
    fun actionsFollowCapabilitiesAndStayUnderFive() {
        assertEquals(listOf(QuickAddAction.TIMER, QuickAddAction.ACTIVITY, QuickAddAction.TASK, QuickAddAction.GRADE, QuickAddAction.EVENT), QuickAddAction.availableFor(Capabilities(Role.STUDENT, false)))
        assertEquals(listOf(QuickAddAction.CHEER, QuickAddAction.ACTIVITY, QuickAddAction.GRADE, QuickAddAction.EVENT), QuickAddAction.availableFor(Capabilities(Role.PARENT, false)))
        assertEquals(listOf(QuickAddAction.CHEER, QuickAddAction.ACTIVITY, QuickAddAction.TASK, QuickAddAction.GRADE, QuickAddAction.EVENT), QuickAddAction.availableFor(Capabilities(Role.PARENT, true)))
        assertTrue(Role.entries.all { QuickAddAction.availableFor(Capabilities(it, true)).size <= QuickAddAction.MAX_ITEMS })
    }

    @Test
    fun everyEventWritesOnceAndLeavesAMessage() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        val vm = vm(); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(QuickAddEvent.Cheer("  잘했어 ")); var s = settle(vm.state)
        assertEquals(listOf("잘했어"), notes.added); assertEquals("격려를 남겼어요", s.savedMessage)
        vm.onEvent(QuickAddEvent.ClearMessage); s = settle(vm.state); assertNull(s.savedMessage)
        vm.onEvent(QuickAddEvent.Cheer("   ")); settle(vm.state); assertEquals(1, notes.added.size)
        vm.onEvent(QuickAddEvent.SaveActivity(Fixtures.activity("과학관"))); settle(vm.state)
        assertEquals("과학관", activities.saved.single().title)
        vm.onEvent(QuickAddEvent.SaveTask(" 익힘책 ", "math", TaskType.HOMEWORK, today, "PARENT")); settle(vm.state)
        assertEquals("익힘책", tasks.saved.single().title); assertEquals("PARENT", tasks.saved.single().createdByRole); assertEquals(today.toEpochDay(), tasks.saved.single().dueDate)
        vm.onEvent(QuickAddEvent.SaveGrade("math", "중간", ExamType.MIDTERM, 88.0, 100.0, 70.0, today, "")); settle(vm.state)
        assertEquals(88.0, grades.saved.single().score, 0.0); assertEquals(70.0, grades.saved.single().classAverage!!, 0.0)
        vm.onEvent(QuickAddEvent.SaveEvent("학원", null, EventType.ACADEMY, today, LocalTime.of(17, 0), LocalTime.of(16, 0), true, "역삼", "")); s = settle(vm.state)
        val e = events.saved.single()
        assertEquals(e.startAt + 60 * 60 * 1000L, e.endAt) // 종료가 시작보다 앞서면 한 시간짜리
        assertTrue(e.repeatWeekly); assertEquals("일정을 추가했어요", s.savedMessage)
        assertEquals(listOf(Fixtures.math), s.subjects)
        job.cancel()
    }
}
