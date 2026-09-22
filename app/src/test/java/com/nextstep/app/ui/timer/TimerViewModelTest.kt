package com.nextstep.app.ui.timer

import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeStudySessionRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class TimerViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams()
    private val sessions = FakeStudySessionRepository()

    private fun vm() = TimerViewModel(streams, sessions, tickMillis = 60_000L)

    /** 틱 흐름이 무한하므로 advanceUntilIdle 대신 현재 큐만 실행합니다. */
    private fun TestScope.settleNow(vm: TimerViewModel): TimerUiState { runCurrent(); return vm.state.value }

    @Test
    fun defaultSelectionIsRunningSubjectThenFirstSubject() = runTest {
        streams.subjects.value = listOf(Fixtures.math, Fixtures.english)
        val vm = vm(); val job = subscribe(vm.state)
        assertEquals("math", settleNow(vm).selectedSubjectId)
        vm.onEvent(TimerEvent.SelectSubject("eng"))
        assertEquals("eng", settleNow(vm).selectedSubjectId)
        job.cancel()
    }

    @Test
    fun startStopCancelDriveRepositoryAndLastSaved() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        val saved = Fixtures.session("math", DateUtils.today(), LocalTime.of(9, 0), 30)
        sessions.stopResult = saved
        val vm = vm(); val job = subscribe(vm.state); settleNow(vm)
        vm.onEvent(TimerEvent.Start)
        assertEquals("math", settleNow(vm).running!!.subjectId)
        vm.onEvent(TimerEvent.Stop)
        val s = settleNow(vm)
        assertNull(s.running); assertEquals(saved.id, s.lastSaved!!.id)
        vm.onEvent(TimerEvent.Start)
        assertNull(settleNow(vm).lastSaved) // 새로 시작하면 저장 안내 제거
        vm.onEvent(TimerEvent.Cancel)
        assertNull(settleNow(vm).running)
        job.cancel()
    }

    @Test
    fun manualEntryBuildsSessionAndIgnoresNonPositiveMinutes() = runTest {
        val vm = vm(); val job = subscribe(vm.state)
        vm.onEvent(TimerEvent.AddManual("math", LocalDate.of(2026, 9, 1), LocalTime.of(19, 0), 0, ""))
        vm.onEvent(TimerEvent.AddManual("math", LocalDate.of(2026, 9, 1), LocalTime.of(19, 0), 45, "메모"))
        vm.onEvent(TimerEvent.Delete("x"))
        settleNow(vm)
        val s = sessions.saved.single()
        assertEquals(45, s.durationMinutes); assertEquals(DateUtils.toMillis(LocalDate.of(2026, 9, 1), LocalTime.of(19, 45)), s.endAt); assertEquals("메모", s.note)
        assertEquals(listOf("x"), sessions.deleted)
        job.cancel()
    }

    @Test
    fun todaySessionsAndMinutesOnlyIncludeToday() = runTest {
        val today = DateUtils.today()
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(8, 0), 20), Fixtures.session("math", today.minusDays(1), LocalTime.of(8, 0), 90))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settleNow(vm)
        assertEquals(20, s.todayMinutes); assertEquals(1, s.todaySessions.size)
        job.cancel()
    }
}
