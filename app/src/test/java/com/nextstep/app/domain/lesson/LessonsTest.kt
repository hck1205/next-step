package com.nextstep.app.domain.lesson

import com.nextstep.app.data.local.entity.LessonEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.testing.Fixtures
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonsTest {
    private val plan = LessonPlan(setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), 16 * 60, 17 * 60 + 30, fee = 300_000, feeDay = 31)
    private val march = YearMonth.of(2029, 3)

    private fun record(date: LocalDate, status: LessonStatus) = LessonEntity(familyId = Fixtures.FAMILY, mentorId = "t", date = date.toEpochDay(), status = status.name)

    @Test
    fun monthListsPlannedDaysAndExtraRecords() {
        val records = listOf(record(LocalDate.of(2029, 3, 6), LessonStatus.DONE), record(LocalDate.of(2029, 3, 8), LessonStatus.ABSENT), record(LocalDate.of(2029, 3, 10), LessonStatus.DONE))
        val days = Lessons.month(plan, records, march)
        assertEquals(10, days.size) // 화·목 9번 + 토요일 보강 1번
        assertTrue(days.single { it.date.dayOfMonth == 10 }.extra)
        assertEquals(LessonStatus.ABSENT, days.single { it.date.dayOfMonth == 8 }.status)
        assertEquals("수업 2/10회 · 결석 1", Lessons.summary(days).line)
    }

    @Test
    fun deletedRecordsAndOtherMonthsAreIgnored() {
        val records = listOf(record(LocalDate.of(2029, 3, 6), LessonStatus.DONE).copy(deleted = true), record(LocalDate.of(2029, 4, 3), LessonStatus.MAKEUP))
        val days = Lessons.month(plan, records, march)
        assertEquals(9, days.size); assertTrue(days.all { it.status == null })
        val mentor = Fixtures.member(Role.MENTOR, "김쌤", id = "t").copy(lessonDays = "2,4")
        val april = Lessons.bookOf(mentor, records, YearMonth.of(2029, 4), LocalDate.of(2029, 4, 3))!!
        assertEquals(LessonStatus.MAKEUP, april.on(LocalDate.of(2029, 4, 3))?.status)
        assertNull(april.on(LocalDate.of(2029, 4, 4)))
        assertNull(Lessons.bookOf(Fixtures.member(Role.MENTOR, "새쌤", id = "new"), records, YearMonth.of(2029, 4), LocalDate.of(2029, 4, 3))) // 일정도 기록도 없음
    }

    @Test
    fun tuitionFallsOnMonthEndAndRollsOver() {
        assertEquals(LocalDate.of(2029, 2, 28), Lessons.tuition(plan, LocalDate.of(2029, 2, 10))?.date) // 31일이 없는 달은 말일
        assertEquals(LocalDate.of(2029, 3, 31), Lessons.tuition(plan, LocalDate.of(2029, 3, 1))?.date)
        assertEquals(LocalDate.of(2029, 5, 31), Lessons.tuition(plan.copy(feeDay = 31), LocalDate.of(2029, 5, 1))?.date)
        assertEquals(LocalDate.of(2029, 4, 10), Lessons.tuition(plan.copy(feeDay = 10), LocalDate.of(2029, 3, 11))?.date)
        assertNull(Lessons.tuition(plan.copy(fee = 0), LocalDate.of(2029, 3, 1)))
    }

    @Test
    fun tuitionLineShowsOnlyNearTheDay() {
        assertNull(Lessons.tuitionLine(plan, LocalDate.of(2029, 3, 27)))
        assertEquals("수업료 받을 날 D-3 · 300,000원", Lessons.tuitionLine(plan, LocalDate.of(2029, 3, 28)))
        assertEquals("수업료 받을 날 오늘 · 300,000원", Lessons.tuitionLine(plan, LocalDate.of(2029, 3, 31)))
    }

    @Test
    fun planReadsAndWritesMemberFields() {
        val m = Fixtures.member(Role.MENTOR, "김쌤").copy(lessonDays = "4, 2,9,x", lessonStart = 960, lessonEnd = 1050, tuitionFee = 5, tuitionDay = 25)
        val p = LessonPlan.of(m)
        assertEquals(setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), p.days); assertEquals(1050, p.endMinute); assertEquals(25, p.feeDay)
        assertEquals("2,4", LessonPlan.encodeDays(p.days))
        assertTrue(p.isSet); assertTrue(!LessonPlan.of(Fixtures.member(Role.MENTOR, "새쌤")).isSet)
        assertNull(LessonStatus.from("??")); assertEquals("보강 필요 1", LessonSummary(1, 0, 0, 1).line.substringAfter(" · "))
    }

    @Test
    fun booksForShowsAMentorOnlyTheirOwnBook() {
        val kim = Fixtures.member(Role.MENTOR, "김쌤", id = "kim").copy(lessonDays = "2")
        val lee = Fixtures.member(Role.MENTOR, "이쌤", id = "lee").copy(lessonDays = "4")
        val all = listOf(kim, lee, Fixtures.member(Role.PARENT, "엄마"))
        assertEquals(listOf("김쌤", "이쌤"), Lessons.booksFor(false, null, all, emptyList(), march, LocalDate.of(2029, 3, 1)).map { it.mentorName })
        assertEquals(listOf("이쌤"), Lessons.booksFor(true, lee, all, emptyList(), march, LocalDate.of(2029, 3, 1)).map { it.mentorName })
        assertTrue(Lessons.booksFor(true, null, all, emptyList(), march, LocalDate.of(2029, 3, 1)).isEmpty())
    }
}
