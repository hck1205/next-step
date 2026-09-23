package com.nextstep.app.ui.activities

import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.FakeActivityRepository
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ActivitiesViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val repo = FakeActivityRepository()
    private val today = LocalDate.of(2029, 10, 10) // 2020-05-15 생 → 초3 2학기

    private fun vm() = ActivitiesViewModel(streams, repo, today = { today })

    @Test
    fun stateGroupsByPeriodNewestFirstAndCountsCurrentAndOngoing() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아이", id = "kid", birthDate = LocalDate.of(2020, 5, 15)))
        streams.activities.value = listOf(
            Fixtures.activity("피아노", ActivityType.HOBBY, LocalDate.of(2028, 4, 1)),
            Fixtures.activity("과학관", ActivityType.FIELD_TRIP, LocalDate.of(2029, 10, 1)),
            Fixtures.activity("로봇반", ActivityType.CLUB, LocalDate.of(2029, 9, 5)),
            Fixtures.activity("삭제", ActivityType.TRAVEL, LocalDate.of(2029, 10, 2)).copy(deleted = true),
        )
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertTrue(s.loaded); assertEquals("g3s2", s.currentPeriodKey); assertEquals(3, s.activities.size)
        assertEquals(2, s.currentPeriodCount); assertEquals(listOf("로봇반", "피아노"), s.ongoing.map { it.title }) // 최신순
        assertEquals(listOf("초3 2학기", "초2 1학기"), s.sections.map { it.first })
        assertEquals(listOf("과학관", "로봇반"), s.sections.first().second.map { it.title })
        vm.onEvent(ActivitiesEvent.SetFilter(ActivityType.HOBBY)); s = settle(vm.state)
        assertEquals(listOf("초2 1학기"), s.sections.map { it.first })
        job.cancel()
    }

    @Test
    fun withoutBirthDateEverythingIsOneSectionAndEventsReachRepository() = runTest {
        streams.activities.value = listOf(Fixtures.activity("소풍"))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(listOf("전체"), s.sections.map { it.first }); assertEquals(0, s.currentPeriodCount)
        vm.onEvent(ActivitiesEvent.Save(Fixtures.activity("새 활동"))); vm.onEvent(ActivitiesEvent.Delete("a-소풍")); settle(vm.state)
        assertEquals(listOf("새 활동"), repo.saved.map { it.title }); assertEquals(listOf("a-소풍"), repo.deleted)
        job.cancel()
    }
}
