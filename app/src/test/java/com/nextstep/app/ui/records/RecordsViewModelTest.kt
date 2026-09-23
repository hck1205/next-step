package com.nextstep.app.ui.records

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.stats.BalanceVerdict
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.data.model.AptitudeDomain
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGrowthRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RecordsViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val growth = FakeGrowthRepository()
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun balanceUsesStageAndCurrentPeriodFromBirthDate() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아이", id = "kid", birthDate = LocalDate.of(2020, 5, 15)))
        streams.activities.value = listOf(Fixtures.activity("과학관", date = LocalDate.of(2029, 10, 1)))
        streams.tasks.value = listOf(Fixtures.task("a", today, by = "STUDENT"))
        val vm = RecordsViewModel(streams, growth, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.loaded); assertEquals(GrowthStage.EARLY_ELEMENTARY, s.stage); assertEquals("초3 2학기", s.currentPeriodLabel)
        val b = s.balance!!
        assertEquals(1, b.experiencesThisPeriod); assertEquals(1f, b.selfDirectedRatio!!, 0f); assertEquals(100, b.recommendedWeekMinutes)
        job.cancel()
    }

    @Test
    fun withoutStudentInfoBalanceStillReportsWithoutVerdict() = runTest {
        val vm = RecordsViewModel(streams, growth, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertNull(s.stage); assertNull(s.currentPeriodLabel)
        assertEquals(BalanceVerdict.NONE, s.balance!!.studyVerdict); assertEquals(0, s.balance!!.experiencesThisPeriod)
        assertEquals(RecordSegment.BALANCE, RecordSegment.from(null)); assertEquals(RecordSegment.GRADES, RecordSegment.from("grades"))
        job.cancel()
    }

    @Test
    fun growthAndAptitudeComeFromStreamsAndEventsWriteToRepository() = runTest {
        streams.growthRecords.value = listOf(Fixtures.growth(LocalDate.of(2029, 3, 1), height = 127.0, id = "a"), Fixtures.growth(LocalDate.of(2029, 9, 1), height = 130.0, visionL = 0.6, visionR = 1.0, id = "b"))
        streams.activities.value = listOf(Fixtures.activity("피아노 학원", ActivityType.HOBBY, LocalDate.of(2028, 8, 1), rating = 5))
        streams.observations.value = listOf(Fixtures.observation(AptitudeDomain.MUSIC, "따라 부른다", 3))
        val vm = RecordsViewModel(streams, growth, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(130.0, s.growth!!.heightCm!!, 0.0); assertTrue(s.growth!!.signals.any { it.title.contains("시력 검진") })
        assertEquals(listOf("b", "a"), s.growthRecords.map { it.id })
        assertEquals(AptitudeDomain.MUSIC, s.aptitude.single().domain); assertEquals(1, s.observations.size)
        vm.onEvent(RecordsEvent.SaveGrowth(Fixtures.growth(today, height = 131.0)))
        vm.onEvent(RecordsEvent.DeleteGrowth("a"))
        vm.onEvent(RecordsEvent.AddObservation(Fixtures.observation(AptitudeDomain.ART, "색을 잘 고른다", 2)))
        vm.onEvent(RecordsEvent.DeleteObservation("o-따라 부른다"))
        settle(vm.state)
        assertEquals(listOf("record:131.0:null:null:null", "deleteRecord:a", "observe:ART:2:색을 잘 고른다", "deleteObservation:o-따라 부른다"), growth.calls)
        job.cancel()
    }
}
