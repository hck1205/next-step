package com.nextstep.app.ui.growthalbum

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GrowthAlbumViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.STUDENT)

    @Test
    fun thisYearOrLastYear() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid"))
        val vm = GrowthAlbumViewModel(streams, today = { LocalDate.of(2029, 11, 1) }); val job = subscribe(vm.state)
        val now = settle(vm.state)
        assertEquals("지우의 2029학년도 성장 앨범", now.album!!.title)
        assertEquals("지우", now.book!!.studentName); assertEquals("2029학년도", now.book!!.year.label); assertTrue(now.book!!.isEmpty)
        vm.onEvent(GrowthAlbumEvent.SetYearsBack(1))
        assertEquals("지우의 2028학년도 성장 앨범", settle(vm.state).album!!.title)
        vm.onEvent(GrowthAlbumEvent.SetYearsBack(5))
        assertEquals(1, settle(vm.state).yearsBack)
        job.cancel()
    }
}
