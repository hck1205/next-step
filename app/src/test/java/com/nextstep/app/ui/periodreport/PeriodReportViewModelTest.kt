package com.nextstep.app.ui.periodreport

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.period.PeriodKind
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PeriodReportViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val today = LocalDate.of(2029, 4, 20)

    @Test
    fun monthOrTermReportAndLongTrends() = runTest {
        streams.profile.value = streams.profile.value.copy(studentName = "지우")
        streams.sessions.value = listOf(Fixtures.session("math", LocalDate.of(2029, 4, 2), LocalTime.of(9, 0), 60))
        val vm = PeriodReportViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals("지우의 월간 리포트", s.report!!.title); assertEquals(listOf("3월", "4월"), s.months.map { it.label })
        vm.onEvent(PeriodReportEvent.SetKind(PeriodKind.TERM))
        val term = settle(vm.state)
        assertEquals("2029학년도 1학기", term.report!!.subtitle); assertTrue(term.report!!.sections[0].lines[0].contains("지난 학기"))
        job.cancel()
    }
}
