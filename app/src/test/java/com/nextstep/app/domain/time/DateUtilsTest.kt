package com.nextstep.app.domain.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

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
        assertEquals("월", DateUtils.dayOfWeekLabel(DayOfWeek.MONDAY))
    }
}
