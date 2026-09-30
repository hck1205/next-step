package com.nextstep.app.domain.notice

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.MilestoneCategory
import com.nextstep.app.domain.lesson.TuitionDue
import com.nextstep.app.domain.stats.UpcomingExam
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NoticeComposerTest {
    private val today = LocalDate.of(2029, 3, 7)
    private val family = listOf(
        Fixtures.familyEvent("수영", today, minutes = 17 * 60 to 18 * 60),
        Fixtures.familyEvent("치과", today.plusDays(1)),
    )

    private fun journey(title: String, due: LocalDate) = JourneyItem(null, "j", title, "", "", MilestoneCategory.ADMIN, null, due.minusMonths(1), due, MilestoneStatus.UPCOMING, 0)

    @Test
    fun morningGathersFamilyExamsJourneyAndTasksInOrder() {
        val input = NoticeInput(
            family = true, isStudent = false, familyAhead = FamilyCalendar.ahead(family, today),
            exams = listOf(UpcomingExam("국어 단원평가", null, today.plusDays(3)), UpcomingExam("수학", null, today.plusDays(5))),
            journey = listOf(journey("유치원 신청", today.plusDays(2)), journey("먼 일", today.plusDays(30))),
            dueToday = 2, overdue = 1,
        )
        val n = NoticeComposer.morning(input, today)!!
        assertEquals(listOf("오늘 17:00 수영", "내일 치과", "국어 단원평가 D-3", "유치원 신청 마감 D-2"), n.lines) // 네 줄까지
        assertEquals("오늘 챙길 것 4가지", n.title)
        val few = NoticeComposer.morning(input.copy(familyAhead = emptyList(), exams = emptyList(), journey = emptyList()), today)!!
        assertEquals(listOf("오늘 할 일 2개 · 밀린 것 1개"), few.lines)
    }

    @Test
    fun studentsHearNoOverdueOrJourneyAndMentorsNoFamily() {
        val student = NoticeInput(family = true, isStudent = true, journey = listOf(journey("유치원 신청", today)), overdue = 3)
        assertNull(NoticeComposer.morning(student, today)) // 밀린 것·여정은 학생에게 알리지 않음
        assertTrue(NoticeComposer.morning(student.copy(dueToday = 1), today)!!.title.endsWith("하나씩 해 봐요"))
        val mentor = NoticeInput(family = false, isStudent = false, familyAhead = FamilyCalendar.ahead(family, today))
        assertNull(NoticeComposer.morning(mentor, today))
    }

    @Test
    fun lessonsAndTuitionGoToAdultsOnly() {
        val due = TuitionDue(today.plusDays(2), 2, 300_000)
        val mentor = NoticeComposer.morning(NoticeInput(family = false, isStudent = false, lessonAt = 16 * 60, tuition = listOf(due)), today)!!
        assertEquals(listOf("오늘 16:00 수업", "수업료 받을 날 D-2 · 300,000원"), mentor.lines)
        val parent = NoticeComposer.morning(NoticeInput(family = true, isStudent = false, tuition = listOf(due, due.copy(daysLeft = 10))), today)!!
        assertEquals(listOf("수업료 낼 날 D-2 · 300,000원"), parent.lines) // 아직 먼 수업료는 알리지 않음
        assertNull(NoticeComposer.morning(NoticeInput(family = true, isStudent = true, lessonAt = 600, tuition = listOf(due)), today)) // 학생에게는 수업료 없음
    }

    @Test
    fun weekendInvitesOnlyFamiliesWhoHaveNotTalkedYet() {
        val nextWeek = listOf(FamilyCalendar.occurrenceOn(Fixtures.familyEvent("캠핑", today.plusDays(6), kind = "OUTING"), today.plusDays(6))!!)
        val n = NoticeComposer.weekend(NoticeInput(family = true, isStudent = true, nextWeek = nextWeek))!!
        assertEquals("주말 이야기 시간이에요", n.title); assertTrue(n.body.contains("다음 주 기대되는 일: 캠핑"))
        assertNull(NoticeComposer.weekend(NoticeInput(family = true, isStudent = true, talkDone = true)))
        assertNull(NoticeComposer.weekend(NoticeInput(family = false, isStudent = false)))
        assertTrue(n.lines.none { it.contains("밀린") })
    }
}
