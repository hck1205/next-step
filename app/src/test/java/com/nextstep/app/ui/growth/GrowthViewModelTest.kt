package com.nextstep.app.ui.growth

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGrowthRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GrowthViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val growth = FakeGrowthRepository()
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun allRecordsGroupedByYearNewestFirstWithHeightTrend() = runTest {
        streams.growthRecords.value = listOf(
            Fixtures.growth(LocalDate.of(2028, 3, 1), height = 124.0, id = "a"),
            Fixtures.growth(LocalDate.of(2029, 3, 1), height = 127.0, id = "b"),
            Fixtures.growth(LocalDate.of(2029, 9, 1), height = 130.0, visionL = 0.6, visionR = 1.0, id = "c"),
            Fixtures.growth(LocalDate.of(2029, 5, 1), weight = 30.0, id = "w"),
            Fixtures.growth(LocalDate.of(2029, 6, 1), height = 128.0, id = "gone").copy(deleted = true),
        )
        val vm = GrowthViewModel(streams, growth, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(listOf(2029, 2028), s.byYear.map { it.first })
        assertEquals(listOf("c", "w", "b"), s.byYear.first().second.map { it.id })
        assertEquals(4, s.recordCount)
        assertEquals(listOf("28.3" to 124.0, "29.3" to 127.0, "29.9" to 130.0), s.heightTrend)
        assertEquals(130.0, s.summary!!.heightCm!!, 0.0); assertTrue(s.summary!!.signals.any { it.title.contains("시력 검진") })
        job.cancel()
    }

    @Test
    fun eventsWriteToRepository() = runTest {
        val vm = GrowthViewModel(streams, growth, today = { today }); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(GrowthEvent.Save(Fixtures.growth(today, height = 131.0)))
        vm.onEvent(GrowthEvent.Delete("a"))
        settle(vm.state)
        assertEquals(listOf("record:131.0:null:null:null", "deleteRecord:a"), growth.calls)
        job.cancel()
    }
}
