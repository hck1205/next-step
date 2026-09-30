package com.nextstep.app.domain.period

import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class PeriodReportsTest {
    private val today = LocalDate.of(2029, 4, 20)

    @Test
    fun periodsAreMonthsAndSchoolTerms() {
        val april = Periods.current(PeriodKind.MONTH, today)
        assertEquals(LocalDate.of(2029, 4, 1), april.start); assertEquals(LocalDate.of(2029, 4, 30), april.end); assertEquals("2029년 4월", april.label)
        assertEquals("2029년 3월", Periods.previous(april).label)
        val term = Periods.current(PeriodKind.TERM, today)
        assertEquals("2029학년도 1학기", term.label); assertEquals(LocalDate.of(2029, 8, 31), term.end)
        val before = Periods.previous(term)
        assertEquals("2028학년도 2학기", before.label); assertEquals(LocalDate.of(2028, 9, 1), before.start); assertEquals(LocalDate.of(2029, 2, 28), before.end)
        assertEquals("2029학년도 2학기", Periods.current(PeriodKind.TERM, LocalDate.of(2030, 1, 10)).label)
    }

    @Test
    fun reportComparesWithThePreviousMonthOnly() {
        val r = PeriodRecords(
            sessions = listOf(Fixtures.session("math", LocalDate.of(2029, 4, 2), LocalTime.of(9, 0), 60), Fixtures.session("math", LocalDate.of(2029, 4, 3), LocalTime.of(9, 0), 30), Fixtures.session("math", LocalDate.of(2029, 3, 5), LocalTime.of(9, 0), 40)),
            tasks = listOf(Fixtures.task("a", LocalDate.of(2029, 4, 5), done = true), Fixtures.task("b", LocalDate.of(2029, 4, 6)), Fixtures.task("c", LocalDate.of(2029, 3, 6), done = true)),
            grades = listOf(Fixtures.grade("math", 90.0, LocalDate.of(2029, 4, 9).toEpochDay()), Fixtures.grade("math", 80.0, LocalDate.of(2029, 3, 9).toEpochDay())),
            subjects = listOf(Fixtures.math),
            cheers = listOf(CheerEntity(familyId = "fam", taskId = "a", taskTitle = "a", kind = "CLAP", createdAt = DateUtils.toMillis(LocalDate.of(2029, 4, 5), LocalTime.NOON))),
        )
        val now = Periods.current(PeriodKind.MONTH, today)
        val s = PeriodReports.stats(now, r)
        assertEquals(90, s.studyMinutes); assertEquals(2, s.studyDays); assertEquals(50, s.doneRate); assertEquals(mapOf("수학" to 90), s.scores); assertEquals(1, s.cheers)
        val doc = PeriodReports.report("지우", now, s, PeriodReports.stats(Periods.previous(now), r))
        assertEquals("지우의 월간 리포트", doc.title); assertEquals("2029년 4월", doc.subtitle)
        assertEquals(listOf("공부 1시간 30분 (지난달 40분)", "공부한 날 2일 (지난달 1일)"), doc.sections[0].lines)
        assertEquals(listOf("마감 할 일 2개 중 1개 끝냄 · 50% (지난달 100%)"), doc.sections[1].lines)
        assertEquals(listOf("수학 90점 (지난달 80점)"), doc.sections[2].lines)
        assertNull(PeriodStats().doneRate)
        val series = PeriodReports.monthlySeries(r, today)
        assertEquals(listOf("3월", "4월"), series.map { it.label }); assertEquals(listOf(40, 90), series.map { it.minutes }); assertEquals(listOf(100, 50), series.map { it.doneRate })
    }
}
