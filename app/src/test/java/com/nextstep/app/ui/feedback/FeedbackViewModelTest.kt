package com.nextstep.app.ui.feedback

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class FeedbackViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.MENTOR)
    private val today = LocalDate.of(2029, 10, 10)
    private fun at(date: LocalDate) = DateUtils.toMillis(date, LocalTime.NOON)

    @Test
    fun mentorSeesOwnAndFamilyNotesByWeek() = runTest {
        streams.myMember.value = Fixtures.member(Role.MENTOR, "쌤")
        streams.notes.value = listOf(
            Fixtures.note("엄마 메모", Role.PARENT, "엄마").copy(createdAt = at(today)),
            Fixtures.note("내 피드백", Role.MENTOR, "쌤").copy(createdAt = at(today.minusDays(1))),
            Fixtures.note("다른 쌤", Role.MENTOR, "다른쌤").copy(createdAt = at(today)),
            Fixtures.note("지난주", Role.STUDENT, "학생").copy(createdAt = at(today.minusDays(7))),
        )
        val vm = FeedbackViewModel(streams, today = { today }); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(listOf("이번 주", "지난주"), s.weeks.map { it.label })
        assertEquals(listOf("엄마 메모", "내 피드백"), s.weeks[0].notes.map { it.text })
        assertEquals(2, s.thisWeekCount); assertEquals(1, s.fromMentors); assertEquals(2, s.fromFamily)
        job.cancel()
    }
}
