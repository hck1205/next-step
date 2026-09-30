package com.nextstep.app.domain.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {
    @Test
    fun weekStartIsMondayOnOrBefore() {
        assertEquals(LocalDate.of(2026, 9, 21), DateUtils.weekStart(LocalDate.of(2026, 9, 27))) // 일요일
        assertEquals(LocalDate.of(2026, 9, 21), DateUtils.weekStart(LocalDate.of(2026, 9, 21))) // 월요일 그대로
        assertEquals(DayOfWeek.MONDAY, DateUtils.weekStart(LocalDate.of(2026, 1, 1)).dayOfWeek)
    }

    @Test
    fun millisRoundTripKeepsDateAndTime() {
        val date = LocalDate.of(2026, 3, 15); val time = LocalTime.of(19, 30)
        val ms = DateUtils.toMillis(date, time)
        assertEquals(date, DateUtils.toLocalDate(ms))
        assertEquals(time, DateUtils.toLocalDateTime(ms).toLocalTime())
        assertEquals(DateUtils.startOfDayMillis(date), DateUtils.toMillis(date, LocalTime.MIDNIGHT))
    }

    @Test
    fun formatMinutesUsesHoursAndMinutes() {
        assertEquals("45분", DateUtils.formatMinutes(45))
        assertEquals("2시간", DateUtils.formatMinutes(120))
        assertEquals("1시간 5분", DateUtils.formatMinutes(65))
        assertEquals("0분", DateUtils.formatMinutes(0))
    }

    @Test
    fun formatElapsedShowsHoursOnlyWhenNeeded() {
        assertEquals("00:05", DateUtils.formatElapsed(5))
        assertEquals("12:34", DateUtils.formatElapsed(12 * 60 + 34))
        assertEquals("1:00:01", DateUtils.formatElapsed(3601))
    }

    @Test
    fun dDayCountsRelativeToGivenDate() {
        val today = LocalDate.of(2026, 9, 22)
        assertEquals("D-Day", DateUtils.dDay(today, today))
        assertEquals("D-3", DateUtils.dDay(today.plusDays(3), today))
        assertEquals("D+2", DateUtils.dDay(today.minusDays(2), today))
    }

    @Test
    fun koreanFormattersProduceExpectedShapes() {
        val d = LocalDate.of(2026, 9, 22)
        assertEquals("9월 22일", DateUtils.formatDate(d))
        assertEquals("9/22", DateUtils.formatShortDate(d))
        assertEquals("2026년 9월", DateUtils.formatMonth(d))
        assertTrue(DateUtils.formatFullDate(d).startsWith("2026년 9월 22일"))
        assertEquals("9월 22일 화", DateUtils.formatDay(d))
        assertEquals("월", DateUtils.dayOfWeekLabel(DayOfWeek.MONDAY))
    }

    @Test
    fun minutesOfTheDayAndWeekRanges() {
        assertEquals("16:00", DateUtils.formatClock(960)); assertEquals("00:30", DateUtils.formatClock(24 * 60 + 30)) // 하루를 넘치면 감음
        assertEquals(1050, DateUtils.minuteOf(DateUtils.timeOfMinute(1050)))
        assertEquals("4/6 – 4/12", DateUtils.formatWeek(LocalDate.of(2026, 4, 6)))
    }
}
