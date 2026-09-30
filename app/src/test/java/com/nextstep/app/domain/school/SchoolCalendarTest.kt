package com.nextstep.app.domain.school

import com.nextstep.app.domain.familycalendar.FamilyCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class SchoolCalendarTest {
    private val school = School("B10", "7010057", "대치초")

    @Test
    fun yearRangeAndGradeInSchool() {
        assertEquals(LocalDate.of(2029, 3, 1) to LocalDate.of(2030, 2, 28), SchoolCalendar.yearRange(LocalDate.of(2029, 9, 1)))
        assertEquals(LocalDate.of(2028, 3, 1) to LocalDate.of(2029, 2, 28), SchoolCalendar.yearRange(LocalDate.of(2029, 2, 10)))
        assertEquals(5, SchoolCalendar.gradeInSchool(5)); assertEquals(2, SchoolCalendar.gradeInSchool(8)); assertEquals(3, SchoolCalendar.gradeInSchool(12)); assertEquals(0, SchoolCalendar.gradeInSchool(0))
        assertEquals("B10:7010057", school.key); assertEquals(school, School.fromKey("B10:7010057", "대치초")); assertNull(School.fromKey("", "x")); assertNull(School.fromKey("B10", "x"))
    }

    @Test
    fun keepsOurGradeMergesRunsAndSetsHeadsUp() {
        val fri = LocalDate.of(2029, 7, 20) // 금
        val days = listOf(
            SchoolDay(fri, "여름방학", dayOff = true), SchoolDay(fri.plusDays(3), "여름방학", dayOff = true), SchoolDay(fri.plusDays(4), "여름방학", dayOff = true),
            SchoolDay(fri.minusDays(1), "토요휴업일", dayOff = true),
            SchoolDay(LocalDate.of(2029, 4, 25), "1학기 중간고사", grades = setOf(5, 6)),
            SchoolDay(LocalDate.of(2029, 5, 2), "6학년 수학여행", grades = setOf(6)),
            SchoolDay(LocalDate.of(2029, 5, 4), "재량휴업일", dayOff = true),
            SchoolDay(LocalDate.of(2029, 5, 10), "학부모 공개수업"),
            SchoolDay(LocalDate.of(2029, 5, 11), " "),
        )
        val events = SchoolCalendar.toFamilyEvents(days, grade = 5, studentId = "kid", school = school)
        assertEquals(listOf("1학기 중간고사", "재량휴업일", "학부모 공개수업", "여름방학"), events.map { it.title }) // 6학년 행사·토요휴업일·빈 이름 빠짐
        val vacation = events.last()
        assertEquals(fri.toEpochDay(), vacation.startDate); assertEquals(fri.plusDays(4).toEpochDay(), vacation.endDate) // 주말을 건너 하나로
        assertEquals(listOf("WEEK", "THREE_DAYS", "DAY_BEFORE", "THREE_DAYS"), events.map { it.headsUp })
        assertEquals(setOf("SCHOOL"), events.map { it.kind }.toSet()); assertEquals("kid", events[0].memberIds); assertEquals(SchoolCalendar.SOURCE, events[0].createdByRole)
        assertEquals(events.map { it.id }, SchoolCalendar.toFamilyEvents(days, 5, "kid", school).map { it.id }) // 다시 받아도 같은 id
        assertEquals(5, FamilyCalendar.month(listOf(vacation), java.time.YearMonth.of(2029, 7)).size)
        assertEquals(5, SchoolCalendar.toFamilyEvents(days, grade = 0, studentId = "kid", school = school).size) // 학년을 모르면 전 학년 행사
    }
}
