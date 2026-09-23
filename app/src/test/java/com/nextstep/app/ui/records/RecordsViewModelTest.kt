package com.nextstep.app.ui.records

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.stats.BalanceVerdict
import com.nextstep.app.fake.FakeFamilyDataStreams
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
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun balanceUsesStageAndCurrentPeriodFromBirthDate() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아이", id = "kid", birthDate = LocalDate.of(2020, 5, 15)))
        streams.activities.value = listOf(Fixtures.activity("과학관", date = LocalDate.of(2029, 10, 1)))
        streams.tasks.value = listOf(Fixtures.task("a", today, by = "STUDENT"))
        val vm = RecordsViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.loaded); assertEquals(GrowthStage.EARLY_ELEMENTARY, s.stage); assertEquals("초3 2학기", s.currentPeriodLabel)
        val b = s.balance!!
        assertEquals(1, b.experiencesThisPeriod); assertEquals(1f, b.selfDirectedRatio!!, 0f); assertEquals(100, b.recommendedWeekMinutes)
        job.cancel()
    }

    @Test
    fun withoutStudentInfoBalanceStillReportsWithoutVerdict() = runTest {
        val vm = RecordsViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertNull(s.stage); assertNull(s.currentPeriodLabel)
        assertEquals(BalanceVerdict.NONE, s.balance!!.studyVerdict); assertEquals(0, s.balance!!.experiencesThisPeriod)
        assertEquals(RecordSegment.BALANCE, RecordSegment.from(null)); assertEquals(RecordSegment.GRADES, RecordSegment.from("grades"))
        job.cancel()
    }
}
