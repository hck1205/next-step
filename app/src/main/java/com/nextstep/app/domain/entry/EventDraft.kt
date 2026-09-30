package com.nextstep.app.domain.entry

import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.LocalTime

/**
 * 일정 입력창에서 적은 값 한 벌. 달력과 기록하기가 같은 규칙(글자 다듬기, 종료가 시작보다 이르면 한 시간짜리)으로
 * 저장하도록 일정으로 바꾸는 법을 여기 한 곳에 둡니다. 가족 id 는 저장소가 채웁니다.
 */
data class EventDraft(
    val title: String,
    val subjectId: String?,
    val type: EventType,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val repeatWeekly: Boolean,
    val location: String,
    val memo: String,
) {
    /** 새 일정이거나, [existing] 을 이 값으로 고친 일정(id 유지). */
    fun toEntity(existing: EventEntity? = null): EventEntity {
        val startAt = DateUtils.toMillis(date, start)
        val endAt = DateUtils.toMillis(date, if (end.isAfter(start)) end else start.plusHours(1))
        val base = existing ?: EventEntity(familyId = "", title = title.trim(), startAt = startAt, endAt = endAt)
        return base.copy(
            title = title.trim(), subjectId = subjectId, type = type, startAt = startAt, endAt = endAt,
            repeatWeekly = repeatWeekly, location = location.trim(), memo = memo.trim(),
        )
    }

    companion object {
        /** 고칠 일정의 값으로 채운 입력창. 새 일정이면 [date] 16~17시. */
        fun of(existing: EventEntity?, date: LocalDate): EventDraft = existing?.let {
            EventDraft(
                it.title, it.subjectId, it.type, DateUtils.toLocalDate(it.startAt),
                DateUtils.toLocalDateTime(it.startAt).toLocalTime(), DateUtils.toLocalDateTime(it.endAt).toLocalTime(),
                it.repeatWeekly, it.location, it.memo,
            )
        } ?: EventDraft("", null, EventType.CLASS, date, DEFAULT_START, DEFAULT_START.plusHours(1), false, "", "")

        private val DEFAULT_START: LocalTime = LocalTime.of(16, 0)
    }
}
