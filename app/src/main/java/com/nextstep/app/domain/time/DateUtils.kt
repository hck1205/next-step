package com.nextstep.app.domain.time

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateUtils {
    private val zone: ZoneId get() = ZoneId.systemDefault()
    val KO: Locale = Locale.KOREAN

    fun today(): LocalDate = LocalDate.now(zone)
    fun LocalDate.toEpochDayLong(): Long = this.toEpochDay()
    fun fromEpochDay(day: Long): LocalDate = LocalDate.ofEpochDay(day)

    fun toMillis(dateTime: LocalDateTime): Long = dateTime.atZone(zone).toInstant().toEpochMilli()
    fun toMillis(date: LocalDate, time: LocalTime): Long = toMillis(LocalDateTime.of(date, time))
    fun toLocalDateTime(millis: Long): LocalDateTime = Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime()
    fun toLocalDate(millis: Long): LocalDate = toLocalDateTime(millis).toLocalDate()
    fun startOfDayMillis(date: LocalDate): Long = toMillis(date, LocalTime.MIDNIGHT)

    /** 이번 주 월요일. */
    fun weekStart(date: LocalDate = today()): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFmt = DateTimeFormatter.ofPattern("M월 d일", KO)
    private val fullDateFmt = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", KO)
    private val monthFmt = DateTimeFormatter.ofPattern("yyyy년 M월", KO)
    private val shortDateFmt = DateTimeFormatter.ofPattern("M/d", KO)

    fun formatTime(millis: Long): String = toLocalDateTime(millis).format(timeFmt)
    fun formatTime(time: LocalTime): String = time.format(timeFmt)
    fun formatDate(date: LocalDate): String = date.format(dateFmt)
    fun formatFullDate(date: LocalDate): String = date.format(fullDateFmt)
    fun formatMonth(date: LocalDate): String = date.format(monthFmt)
    fun formatShortDate(date: LocalDate): String = date.format(shortDateFmt)
    fun dayOfWeekLabel(day: DayOfWeek): String = day.getDisplayName(TextStyle.SHORT, KO)

    fun formatMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "${m}분"
            m == 0 -> "${h}시간"
            else -> "${h}시간 ${m}분"
        }
    }

    fun formatElapsed(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format(Locale.ROOT, "%d:%02d:%02d", h, m, s) else String.format(Locale.ROOT, "%02d:%02d", m, s)
    }

    /** D-day 표기. 오늘이면 "D-Day", 지났으면 "D+n". */
    fun dDay(target: LocalDate, from: LocalDate = today()): String {
        val diff = target.toEpochDay() - from.toEpochDay()
        return when {
            diff == 0L -> "D-Day"
            diff > 0 -> "D-$diff"
            else -> "D+${-diff}"
        }
    }
}
