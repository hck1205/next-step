package com.nextstep.app.domain.familycalendar

import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * 가족 달력 계산 한 곳: 어느 날에 어떤 일정이 걸리는지(반복 · 여러 날), 오늘 화면에 미리 띄울 것, 누구의 일정인지 문구.
 * 반복하는 일정은 첫 회차의 날짜를 기준으로 매주(같은 요일) · 매달(같은 날, 그날이 없는 달은 건너뜀) · 매년(같은 달·날) 되풀이합니다.
 */
object FamilyCalendar {

    /** [date] 에 걸린 일정. 하루 종일이 먼저, 그다음 시작 시각 순. */
    fun on(events: List<FamilyEventEntity>, date: LocalDate): List<FamilyOccurrence> =
        events.filter { !it.deleted }.mapNotNull { occurrenceOn(it, date) }
            .sortedWith(compareBy<FamilyOccurrence>({ !it.event.allDay }, { it.event.startMinute }, { it.event.title }))

    /** 한 달의 날마다 걸린 일정(없는 날은 빠짐). */
    fun month(events: List<FamilyEventEntity>, month: YearMonth): Map<LocalDate, List<FamilyOccurrence>> =
        (1..month.lengthOfMonth()).map { month.atDay(it) }.associateWith { on(events, it) }.filterValues { it.isNotEmpty() }

    /** [event] 가 [date] 에 걸리면 그 회차. */
    fun occurrenceOn(event: FamilyEventEntity, date: LocalDate): FamilyOccurrence? {
        val span = spanDays(event)
        return (0L..span).firstNotNullOfOrNull { offset ->
            val start = date.minusDays(offset)
            if (startsOn(event, start)) FamilyOccurrence(event, date, start, start.plusDays(span)) else null
        }
    }

    /** [day] 가 [event] 의 한 회차가 시작하는 날인지. */
    fun startsOn(event: FamilyEventEntity, day: LocalDate): Boolean {
        val first = DateUtils.fromEpochDay(event.startDate)
        val until = event.repeatUntil?.let(DateUtils::fromEpochDay)
        if (day < first || (until != null && day > until)) return false
        return when (FamilyRepeat.from(event.repeat)) {
            FamilyRepeat.NONE -> day == first
            FamilyRepeat.WEEKLY -> ChronoUnit.DAYS.between(first, day) % DAYS_IN_WEEK == 0L
            FamilyRepeat.MONTHLY -> day.dayOfMonth == first.dayOfMonth
            FamilyRepeat.YEARLY -> day.monthValue == first.monthValue && day.dayOfMonth == first.dayOfMonth
        }
    }

    /**
     * 오늘 화면의 "가족 일정": 오늘 걸린 것, 그리고 앞으로 [MAX_AHEAD_DAYS] 일 안에 시작하는 회차 가운데
     * 그 일정의 미리 보기([FamilyHeadsUp]) 안에 든 것. 날짜 → 시각 순이고 한 회차는 한 번만.
     */
    fun ahead(events: List<FamilyEventEntity>, today: LocalDate): List<FamilyOccurrence> {
        val now = on(events, today)
        val later = (1L..MAX_AHEAD_DAYS).flatMap { d ->
            on(events, today.plusDays(d)).filter { it.isFirstDay && d <= FamilyHeadsUp.from(it.event.headsUp).days }
        }
        return (now + later).distinctBy { it.event.id to it.start }
    }

    /** [memberId] 의 일정인지(누구로 정하지 않은 일정은 가족 모두의 것). */
    fun involves(event: FamilyEventEntity, memberId: String): Boolean = event.memberIdList.isEmpty() || memberId in event.memberIdList

    /** 누구의 일정인지 한 줄: 정한 사람이 없거나 모두 떠났으면 "가족 모두". */
    fun who(event: FamilyEventEntity, members: List<MemberEntity>): String =
        event.memberIdList.mapNotNull { id -> members.firstOrNull { it.id == id && !it.deleted }?.name }.joinToString(" · ").ifEmpty { EVERYONE }

    /** 챙기는 사람의 이름(없으면 null). */
    fun keeper(event: FamilyEventEntity, members: List<MemberEntity>): String? =
        event.keeperId.takeIf { it.isNotEmpty() }?.let { id -> members.firstOrNull { it.id == id && !it.deleted }?.name }

    /** 때 한 줄: 여러 날이면 "9/28–9/30", 하루 종일이면 "하루 종일", 아니면 "16:00–17:00". */
    fun timeLabel(occurrence: FamilyOccurrence): String {
        val e = occurrence.event
        val days = if (occurrence.isMultiDay) "${DateUtils.formatShortDate(occurrence.start)}–${DateUtils.formatShortDate(occurrence.end)}" else null
        val hours = if (e.allDay) null else "${DateUtils.formatClock(e.startMinute)}–${DateUtils.formatClock(e.endMinute)}"
        return listOfNotNull(days, hours).joinToString(" ").ifEmpty { ALL_DAY }
    }

    /** 오늘 화면에 붙는 날 이름: "오늘" · "내일" · "9/30 (수)". */
    fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
        today -> "오늘"
        today.plusDays(1) -> "내일"
        else -> "${DateUtils.formatShortDate(date)} (${DateUtils.dayOfWeekLabel(date.dayOfWeek)})"
    }

    /** 가족 구성원(멘토 제외). 달력의 "누구" 고르기에 씁니다. */
    fun family(members: List<MemberEntity>): List<MemberEntity> = members.filter { !it.deleted && !it.isMentor }

    private fun spanDays(event: FamilyEventEntity): Long = (event.endDate - event.startDate).coerceIn(0L, MAX_SPAN_DAYS)

    const val EVERYONE = "가족 모두"
    const val ALL_DAY = "하루 종일"
    /** 오늘 화면이 미리 보는 가장 먼 날(미리 보기의 가장 긴 값). */
    val MAX_AHEAD_DAYS: Long = FamilyHeadsUp.entries.maxOf { it.days }.toLong()
    /** 한 회차가 이어질 수 있는 가장 긴 날 수(여행 등). */
    const val MAX_SPAN_DAYS = 60L
    private const val DAYS_IN_WEEK = 7L
}
