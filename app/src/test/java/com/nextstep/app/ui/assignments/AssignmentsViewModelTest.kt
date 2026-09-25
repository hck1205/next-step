package com.nextstep.app.ui.assignments

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AssignmentsViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.MENTOR)
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun reportCoversMentorAssignments() = runTest {
        streams.subjects.value = listOf(Fixtures.math)
        streams.tasks.value = listOf(Fixtures.task("과제", today.minusDays(1), "math", by = "MENTOR"), Fixtures.task("내 것", today, "math"))
        val vm = AssignmentsViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(1, s.report!!.total)
        assertEquals(listOf("과제"), s.report!!.overdue.map { it.title })
        assertEquals(listOf(Fixtures.math), s.subjects)
        job.cancel()
    }
}
