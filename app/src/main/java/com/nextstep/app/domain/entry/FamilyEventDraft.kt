package com.nextstep.app.domain.entry

import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.domain.familycalendar.FamilyEventKind
import com.nextstep.app.domain.familycalendar.FamilyHeadsUp
import com.nextstep.app.domain.familycalendar.FamilyRepeat
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.LocalTime

/**
 * 가족 일정 입력창에서 적은 값 한 벌. 저장 규칙(글자 다듬기 · 끝이 시작보다 이르면 바로잡기 · 준비물 줄 나누기 · 반복 끝 날)을 여기 한 곳에 둡니다.
 * 가족 id 와 만든 사람은 저장소가 채웁니다.
 */
data class FamilyEventDraft(
    val title: String,
    val kind: FamilyEventKind,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val allDay: Boolean,
    val start: LocalTime,
    val end: LocalTime,
    /** 누구의 일정(비면 가족 모두). */
    val memberIds: List<String>,
    /** 챙기는 사람(데려다주기·예약 등). */
    val keeperId: String?,
    val repeat: FamilyRepeat,
    val repeatUntil: LocalDate?,
    val headsUp: FamilyHeadsUp,
    val location: String,
    /** 준비물: 쉼표나 줄바꿈으로 나눕니다. */
    val bring: String,
    val memo: String,
) {
    val canSave: Boolean get() = title.isNotBlank()

    /** 종류를 바꿉니다. 새 일정이면 그 종류에 흔한 값도 함께(생일·기념일 = 하루 종일 · 매년, 나들이·여행 = 하루 종일). */
    fun withKind(kind: FamilyEventKind, isNew: Boolean): FamilyEventDraft = when {
        !isNew -> copy(kind = kind)
        kind == FamilyEventKind.CELEBRATION -> copy(kind = kind, allDay = true, repeat = FamilyRepeat.YEARLY, headsUp = FamilyHeadsUp.WEEK)
        kind == FamilyEventKind.OUTING -> copy(kind = kind, allDay = true, headsUp = FamilyHeadsUp.THREE_DAYS)
        else -> copy(kind = kind)
    }

    /** 누구의 일정에서 [memberId] 를 넣거나 뺍니다(아무도 없으면 가족 모두). */
    fun toggleMember(memberId: String): FamilyEventDraft = copy(memberIds = if (memberId in memberIds) memberIds - memberId else memberIds + memberId)

    /** 시작 날을 바꿉니다. 끝 날이 그보다 이르면 같이 옮깁니다. */
    fun withStartDate(date: LocalDate): FamilyEventDraft = copy(startDate = date, endDate = if (endDate.isBefore(date)) date else endDate)

    /** 새 일정이거나, [existing] 을 이 값으로 고친 일정(id·만든 사람 유지). */
    fun toEntity(existing: FamilyEventEntity? = null): FamilyEventEntity {
        val last = if (endDate.isBefore(startDate)) startDate else endDate
        val finish = if (!allDay && last == startDate && !end.isAfter(start)) minOf(start.plusHours(1), LATEST) else end
        val base = existing ?: FamilyEventEntity(familyId = "", title = "", startDate = startDate.toEpochDay())
        return base.copy(
            title = title.trim(), kind = kind.name, startDate = startDate.toEpochDay(), endDate = last.toEpochDay(), allDay = allDay,
            startMinute = if (allDay) 0 else start.toSecondOfDay() / SECONDS_IN_MINUTE, endMinute = if (allDay) 0 else finish.toSecondOfDay() / SECONDS_IN_MINUTE,
            memberIds = memberIds.distinct().joinToString(","), keeperId = keeperId.orEmpty(),
            repeat = repeat.name, repeatUntil = repeatUntil?.takeIf { repeat != FamilyRepeat.NONE && !it.isBefore(startDate) }?.toEpochDay(),
            headsUp = headsUp.name, location = location.trim(), bring = items(bring).joinToString("\n"), memo = memo.trim(),
        )
    }

    companion object {
        /** 고칠 일정의 값으로 채운 입력창. 새 일정이면 [date] 10~11시, 가족 모두, 전날부터 미리 보기. */
        fun of(existing: FamilyEventEntity?, date: LocalDate): FamilyEventDraft = existing?.let {
            FamilyEventDraft(
                it.title, FamilyEventKind.from(it.kind), DateUtils.fromEpochDay(it.startDate), DateUtils.fromEpochDay(it.endDate), it.allDay,
                time(it.startMinute), time(it.endMinute), it.memberIdList, it.keeperId.ifEmpty { null },
                FamilyRepeat.from(it.repeat), it.repeatUntil?.let(DateUtils::fromEpochDay), FamilyHeadsUp.from(it.headsUp),
                it.location, it.bringList.joinToString(", "), it.memo,
            )
        } ?: FamilyEventDraft(
            "", FamilyEventKind.FAMILY, date, date, false, DEFAULT_START, DEFAULT_START.plusHours(1), emptyList(), null,
            FamilyRepeat.NONE, null, FamilyHeadsUp.DAY_BEFORE, "", "", "",
        )

        /** 준비물 글을 항목으로(쉼표·줄바꿈, 빈 것·겹치는 것 빼기). */
        fun items(text: String): List<String> = text.split(',', '\n').map { it.trim() }.filter { it.isNotEmpty() }.distinct()

        private fun time(minute: Int): LocalTime = LocalTime.ofSecondOfDay((minute.coerceIn(0, LAST_MINUTE) * SECONDS_IN_MINUTE).toLong())

        private val DEFAULT_START: LocalTime = LocalTime.of(10, 0)
        private val LATEST: LocalTime = LocalTime.of(23, 59)
        private const val SECONDS_IN_MINUTE = 60
        private const val LAST_MINUTE = 23 * 60 + 59
    }
}
