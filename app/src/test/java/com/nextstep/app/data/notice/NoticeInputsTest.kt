package com.nextstep.app.data.notice

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.MentorScopedStreams
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.testing.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticeInputsTest {
    private val today = DateUtils.today()

    @Test
    fun readsWhatThisViewerSeesOnce() = runTest {
        val streams = FakeFamilyDataStreams(role = Role.PARENT).apply {
            familyEvents.value = listOf(Fixtures.familyEvent("외식", today), Fixtures.familyEvent("캠핑", DateUtils.weekStart(today).plusWeeks(1).plusDays(5)))
            tasks.value = listOf(Fixtures.task("오늘", today), Fixtures.task("지난", today.minusDays(2)), Fixtures.task("끝", today, done = true))
        }
        val input = NoticeInputs.of(streams, today)
        assertTrue(input.family); assertFalse(input.isStudent)
        assertEquals(listOf("외식"), input.familyAhead.map { it.event.title }); assertEquals(listOf("캠핑"), input.nextWeek.map { it.event.title })
        assertEquals(1, input.dueToday); assertEquals(1, input.overdue)
        // 멘토: 가족이 아니고 가족 일정도 넘어오지 않음
        val mentor = NoticeInputs.of(MentorScopedStreams(FakeFamilyDataStreams(role = Role.MENTOR).apply { familyEvents.value = streams.familyEvents.value }), today)
        assertFalse(mentor.family); assertTrue(mentor.familyAhead.isEmpty())
    }
}
