package com.nextstep.app.ui.familytalk

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeWeekPlanRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyTalkViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val plans = FakeWeekPlanRepository(streams, role = "PARENT")
    private val saturday = LocalDate.of(2029, 3, 10)
    private val monday = LocalDate.of(2029, 3, 5)

    @Test
    fun gathersTheWeekAndSavesTheTalk() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid", gradeYear = 5), Fixtures.member(Role.PARENT, "엄마", id = "mom"))
        streams.tasks.value = listOf(Fixtures.task("분수", monday, done = true).copy(doneAt = DateUtils.toMillis(monday.plusDays(1), LocalTime.NOON)))
        streams.familyEvents.value = listOf(Fixtures.familyEvent("캠핑", monday.plusWeeks(1).plusDays(5), kind = "OUTING"))
        val vm = FamilyTalkViewModel(streams, plans, today = { saturday }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(monday, s.week); assertEquals(1, s.highlights.doneTasks); assertEquals(listOf("분수"), s.proudIdeas)
        assertEquals(listOf("캠핑"), s.lookForward.map { it.event.title }); assertEquals("지우", s.studentName); assertFalse(s.talked)
        assertTrue(s.past.isEmpty())
        vm.onEvent(FamilyTalkEvent.Save("분수", "자전거 타기", "보드게임 밤"))
        val after = settle(vm.state)
        assertTrue(after.talked); assertEquals("자전거 타기", after.saved!!.wish)
        assertEquals(listOf("talk:$monday:분수:자전거 타기:보드게임 밤"), plans.calls)
        job.cancel()
    }
}
