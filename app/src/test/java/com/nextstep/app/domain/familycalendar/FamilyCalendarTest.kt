package com.nextstep.app.domain.familycalendar

import com.nextstep.app.data.model.Role
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyCalendarTest {
    private val day = LocalDate.of(2029, 1, 31)

    @Test
    fun oneOffAndMultiDayEventsSitOnTheirDays() {
        val trip = Fixtures.familyEvent("제주", day, end = day.plusDays(2), kind = "OUTING")
        val dentist = Fixtures.familyEvent("치과", day.plusDays(1), minutes = 16 * 60 to 17 * 60)
        val events = listOf(trip, dentist, Fixtures.familyEvent("지움", day).copy(deleted = true))
        assertEquals(listOf("제주"), FamilyCalendar.on(events, day).map { it.event.title })
        // 하루 종일이 먼저, 그다음 시각
        assertEquals(listOf("제주", "치과"), FamilyCalendar.on(events, day.plusDays(1)).map { it.event.title })
        val second = FamilyCalendar.on(events, day.plusDays(1)).first()
        assertTrue(second.isMultiDay); assertFalse(second.isFirstDay); assertEquals(day, second.start); assertEquals(day.plusDays(2), second.end)
        assertTrue(FamilyCalendar.on(events, day.plusDays(3)).isEmpty())
        assertEquals("1/31–2/2", FamilyCalendar.timeLabel(second))
        assertEquals("16:00–17:00", FamilyCalendar.timeLabel(FamilyCalendar.on(events, day.plusDays(1))[1]))
        assertEquals(FamilyCalendar.ALL_DAY, FamilyCalendar.timeLabel(FamilyCalendar.occurrenceOn(Fixtures.familyEvent("생일", day), day)!!))
    }

    @Test
    fun repeatsWeeklyMonthlyYearlyUntilTheirEnd() {
        val swim = Fixtures.familyEvent("수영", day, repeat = "WEEKLY", until = day.plusWeeks(2))
        assertTrue(FamilyCalendar.startsOn(swim, day.plusWeeks(2))); assertFalse(FamilyCalendar.startsOn(swim, day.plusWeeks(3)))
        assertFalse(FamilyCalendar.startsOn(swim, day.minusWeeks(1))); assertFalse(FamilyCalendar.startsOn(swim, day.plusDays(3)))
        // 매달 31일: 31일이 없는 달은 건너뜀
        val rent = Fixtures.familyEvent("관리비", day, repeat = "MONTHLY")
        assertTrue(FamilyCalendar.month(listOf(rent), YearMonth.of(2029, 2)).isEmpty())
        assertEquals(listOf(LocalDate.of(2029, 3, 31)), FamilyCalendar.month(listOf(rent), YearMonth.of(2029, 3)).keys.toList())
        val birthday = Fixtures.familyEvent("할머니 생신", LocalDate.of(2020, 5, 8), repeat = "YEARLY", kind = "CELEBRATION")
        assertTrue(FamilyCalendar.startsOn(birthday, LocalDate.of(2029, 5, 8))); assertFalse(FamilyCalendar.startsOn(birthday, LocalDate.of(2029, 6, 8)))
        assertEquals(FamilyEventKind.CELEBRATION, FamilyCalendar.occurrenceOn(birthday, LocalDate.of(2031, 5, 8))!!.kind)
    }

    @Test
    fun aheadShowsTodayAndWhatIsWithinEachHeadsUp() {
        val events = listOf(
            Fixtures.familyEvent("오늘 장보기", day, headsUp = "SAME_DAY"),
            Fixtures.familyEvent("내일 치과", day.plusDays(1), headsUp = "DAY_BEFORE"),
            Fixtures.familyEvent("모레 결혼식", day.plusDays(2), headsUp = "DAY_BEFORE"),
            Fixtures.familyEvent("엿새 뒤 여행", day.plusDays(6), end = day.plusDays(8), headsUp = "WEEK"),
            Fixtures.familyEvent("어제부터 캠프", day.minusDays(1), end = day.plusDays(1), headsUp = "SAME_DAY"),
        )
        assertEquals(listOf("어제부터 캠프", "오늘 장보기", "내일 치과", "엿새 뒤 여행"), FamilyCalendar.ahead(events, day).map { it.event.title })
        assertEquals(FamilyHeadsUp.WEEK.days.toLong(), FamilyCalendar.MAX_AHEAD_DAYS)
    }

    @Test
    fun whoKeeperAndDayLabels() {
        val kid = Fixtures.member(Role.STUDENT, "지우", id = "kid"); val mom = Fixtures.member(Role.PARENT, "엄마", id = "mom")
        val mentor = Fixtures.member(Role.MENTOR, "쌤", id = "t")
        val members = listOf(kid, mom, mentor)
        val e = Fixtures.familyEvent("치과", day, members = "kid,gone", keeper = "mom")
        assertEquals("지우", FamilyCalendar.who(e, members)); assertEquals("엄마", FamilyCalendar.keeper(e, members))
        assertEquals(FamilyCalendar.EVERYONE, FamilyCalendar.who(Fixtures.familyEvent("외식", day), members))
        assertEquals(FamilyCalendar.EVERYONE, FamilyCalendar.who(Fixtures.familyEvent("x", day, members = "gone"), members))
        assertNull(FamilyCalendar.keeper(Fixtures.familyEvent("외식", day), members))
        assertTrue(FamilyCalendar.involves(e, "kid")); assertFalse(FamilyCalendar.involves(e, "mom")); assertTrue(FamilyCalendar.involves(Fixtures.familyEvent("외식", day), "mom"))
        assertEquals(listOf("지우", "엄마"), FamilyCalendar.family(members).map { it.name })
        assertEquals("오늘", FamilyCalendar.dayLabel(day, day)); assertEquals("내일", FamilyCalendar.dayLabel(day.plusDays(1), day))
        assertEquals("2/2 (금)", FamilyCalendar.dayLabel(day.plusDays(2), day))
    }
}
