package com.nextstep.app.ui.kidme

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class KidMeViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.STUDENT)
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun stickerBoardAndRecentActivitiesNewestFirst() = runTest {
        streams.sessions.value = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 10))
        streams.activities.value = (1..8).map { Fixtures.activity("활동 $it", date = today.minusDays(it.toLong())) }
        val vm = KidMeViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals("학생", s.studentName)
        assertEquals(1 + 8, s.board!!.stickers)
        assertEquals(listOf("활동 1", "활동 2", "활동 3", "활동 4", "활동 5", "활동 6"), s.recentActivities.map { it.title })
        job.cancel()
    }
}
