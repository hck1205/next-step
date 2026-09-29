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
        assertEquals(1, input.dueToday); assertEquals(1, input.overdue); assertFalse(input.talkDone)
        streams.weekPlans.value = listOf(com.nextstep.app.data.local.entity.WeekPlanEntity(familyId = "fam", weekStart = com.nextstep.app.domain.familytalk.FamilyTalk.talkWeek(today).toEpochDay(), talkAt = 1L))
        assertTrue(NoticeInputs.of(streams, today).talkDone)
        // 멘토: 가족이 아니고 가족 일정도 넘어오지 않음
        val mentor = NoticeInputs.of(MentorScopedStreams(FakeFamilyDataStreams(role = Role.MENTOR).apply { familyEvents.value = streams.familyEvents.value }), today)
        assertFalse(mentor.family); assertTrue(mentor.familyAhead.isEmpty())
    }

    @Test
    fun lessonsAndTuitionForAdults() = runTest {
        val kim = Fixtures.member(Role.MENTOR, "김쌤", id = "me").copy(lessonDays = "${today.dayOfWeek.value}", lessonStart = 960, lessonEnd = 1050, tuitionFee = 100, tuitionDay = today.dayOfMonth)
        val mentor = NoticeInputs.of(FakeFamilyDataStreams(role = Role.MENTOR).apply { myMember.value = kim; members.value = listOf(kim) }, today)
        assertEquals(960, mentor.lessonAt); assertEquals(0, mentor.tuition.single().daysLeft)
        val parent = NoticeInputs.of(FakeFamilyDataStreams(role = Role.PARENT).apply { members.value = listOf(kim) }, today)
        assertEquals(null, parent.lessonAt); assertEquals(1, parent.tuition.size) // 학부모는 수업료 낼 날만
        val student = NoticeInputs.of(FakeFamilyDataStreams(role = Role.STUDENT).apply { members.value = listOf(kim) }, today)
        assertTrue(student.tuition.isEmpty())
    }
}
