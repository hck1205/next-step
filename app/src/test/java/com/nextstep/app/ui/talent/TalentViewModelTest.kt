package com.nextstep.app.ui.talent

import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.data.model.AptitudeDomain
import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGrowthRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TalentViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val growth = FakeGrowthRepository()
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun signalsAndAllObservationsGroupedByDomain() = runTest {
        streams.activities.value = listOf(Fixtures.activity("피아노 학원", ActivityType.HOBBY, LocalDate.of(2028, 8, 1), rating = 5))
        streams.observations.value = listOf(
            Fixtures.observation(AptitudeDomain.MUSIC, "따라 부른다", 3),
            Fixtures.observation(AptitudeDomain.MUSIC, "박자를 맞춘다", 2, date = LocalDate.of(2029, 10, 5)),
            Fixtures.observation(AptitudeDomain.ART, "색을 잘 고른다", 2),
            Fixtures.observation(AptitudeDomain.ART, "지운 것", 1).copy(deleted = true),
        )
        val vm = TalentViewModel(streams, growth, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(AptitudeDomain.MUSIC, s.signals.first().domain)
        assertEquals(listOf(AptitudeDomain.MUSIC, AptitudeDomain.ART), s.byDomain.map { it.first })
        assertEquals(listOf("박자를 맞춘다", "따라 부른다"), s.byDomain.first().second.map { it.text })
        assertEquals(3, s.observationCount)
        job.cancel()
    }

    @Test
    fun eventsWriteToRepository() = runTest {
        val vm = TalentViewModel(streams, growth, today = { today }); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(TalentEvent.Observe(Fixtures.observation(AptitudeDomain.ART, "색을 잘 고른다", 2)))
        vm.onEvent(TalentEvent.Delete("o-따라 부른다"))
        settle(vm.state)
        assertEquals(listOf("observe:ART:2:색을 잘 고른다", "deleteObservation:o-따라 부른다"), growth.calls)
        job.cancel()
    }
}
