package com.nextstep.app.ui.habits

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.stats.DayPart
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class HabitsViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.MENTOR)
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun reportComesFromSessions() = runTest {
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(7, 0), 30), Fixtures.session("math", today.minusDays(1), LocalTime.of(8, 0), 20))
        val vm = HabitsViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.loaded)
        assertEquals(DayPart.MORNING, s.report!!.bestPart)
        assertEquals(2, s.report!!.currentStreak)
        job.cancel()
    }
}
