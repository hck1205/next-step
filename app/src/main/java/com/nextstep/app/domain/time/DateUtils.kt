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
    /** 기기의 시간대. 날짜를 가르는 모든 계산의 기본값(테스트는 다른 시간대를 넘길 수 있음). */
    val zone: ZoneId get() = ZoneId.systemDefault()
    val KO: Locale = Locale.KOREAN

    fun today(): LocalDate = LocalDate.now(zone)
    /** 지금 시각(분 단위, 초는 버림). 입력 창의 기본값용. */
    fun nowTime(): LocalTime = LocalTime.now(zone).withSecond(0).withNano(0)
    fun fromEpochDay(day: Long): LocalDate = LocalDate.ofEpochDay(day)

    fun toMillis(dateTime: LocalDateTime): Long = dateTime.atZone(zone).toInstant().toEpochMilli()
    fun toMillis(date: LocalDate, time: LocalTime): Long = toMillis(LocalDateTime.of(date, time))
    fun toLocalDateTime(millis: Long): LocalDateTime = Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime()
    fun toLocalDate(millis: Long): LocalDate = toLocalDateTime(millis).toLocalDate()
    /** 시간대를 정해 날짜로(테스트에서 시간대를 고정할 때). */
    fun toLocalDate(millis: Long, zone: ZoneId): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    fun startOfDayMillis(date: LocalDate): Long = toMillis(date, LocalTime.MIDNIGHT)

    /** 이번 주 월요일. */
    fun weekStart(date: LocalDate = today()): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFmt = DateTimeFormatter.ofPattern("M월 d일", KO)
    private val fullDateFmt = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", KO)
    private val dayFmt = DateTimeFormatter.ofPattern("M월 d일 E", KO)
    private val monthFmt = DateTimeFormatter.ofPattern("yyyy년 M월", KO)
    private val shortDateFmt = DateTimeFormatter.ofPattern("M/d", KO)

    fun formatTime(millis: Long): String = toLocalDateTime(millis).format(timeFmt)
    fun formatTime(time: LocalTime): String = time.format(timeFmt)
    fun formatDate(date: LocalDate): String = date.format(dateFmt)

    /** 하루 안의 분(0~1439, 넘치면 하루로 감음) ↔ 시각. 가족 일정·수업 일정이 시각을 분으로 저장합니다. */
    fun timeOfMinute(minute: Int): LocalTime = LocalTime.of(minute / MINUTES_IN_HOUR % HOURS_IN_DAY, minute % MINUTES_IN_HOUR)
    fun minuteOf(time: LocalTime): Int = time.hour * MINUTES_IN_HOUR + time.minute

    /** 하루 안의 분을 "16:00" 으로. */
    fun formatClock(minute: Int): String = formatTime(timeOfMinute(minute))

    /** 한 주(월~일)를 "4/6 – 4/12" 로. */
    fun formatWeek(monday: LocalDate): String = "${formatShortDate(monday)} – ${formatShortDate(monday.plusDays(DAYS_IN_WEEK - 1))}"
    fun formatFullDate(date: LocalDate): String = date.format(fullDateFmt)

    /** 상단 바 제목 옆에 붙는 짧은 날짜(예: "4월 14일 화"). 연도는 뺍니다. */
    fun formatDay(date: LocalDate): String = date.format(dayFmt)
    fun formatMonth(date: LocalDate): String = date.format(monthFmt)
    fun formatShortDate(date: LocalDate): String = date.format(shortDateFmt)
    fun dayOfWeekLabel(day: DayOfWeek): String = day.getDisplayName(TextStyle.SHORT, KO)

    fun formatMinutes(minutes: Int): String {
        val h = minutes / MINUTES_IN_HOUR
        val m = minutes % MINUTES_IN_HOUR
        return when {
            h == 0 -> "${m}분"
            m == 0 -> "${h}시간"
            else -> "${h}시간 ${m}분"
        }
    }

    fun formatElapsed(seconds: Long): String {
        val h = seconds / SECONDS_IN_HOUR
        val m = (seconds % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE
        val s = seconds % SECONDS_IN_MINUTE
        return if (h > 0) String.format(Locale.ROOT, "%d:%02d:%02d", h, m, s) else String.format(Locale.ROOT, "%02d:%02d", m, s)
    }

    /** D-day 표기. 오늘이면 "D-Day", 지났으면 "D+n". */
    fun dDay(target: LocalDate, from: LocalDate = today()): String = dDay((target.toEpochDay() - from.toEpochDay()).toInt())

    /**
     * 남은 날로 D-day 표기: "D-3". 오늘이면 [todayLabel](기본 "D-Day"), 지났으면 [pastLabel](없으면 "D+n").
     * 알림·오늘 카드처럼 말로 쓰는 곳은 "오늘"·"지남"을 넘깁니다.
     */
    fun dDay(daysLeft: Int, todayLabel: String = D_DAY, pastLabel: String? = null): String = when {
        daysLeft == 0 -> todayLabel
        daysLeft > 0 -> "D-$daysLeft"
        else -> pastLabel ?: "D+${-daysLeft}"
    }

    private const val D_DAY = "D-Day"

    /** 한 시간의 분. 분을 시간으로 바꾸는 모든 곳이 씁니다. */
    const val MINUTES_IN_HOUR = 60
    private const val HOURS_IN_DAY = 24
    private const val SECONDS_IN_MINUTE = 60L
    private const val SECONDS_IN_HOUR = 3_600L
    private const val DAYS_IN_WEEK = 7L
}
