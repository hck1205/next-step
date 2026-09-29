package com.nextstep.app.domain.familycalendar

import com.nextstep.app.data.local.entity.FamilyEventEntity
import java.time.LocalDate

/** 가족 일정이 [date] 에 걸린 한 번. 여러 날 일정이면 그 회차의 첫날([start])과 끝날([end]). */
data class FamilyOccurrence(val event: FamilyEventEntity, val date: LocalDate, val start: LocalDate, val end: LocalDate) {
    val kind: FamilyEventKind get() = FamilyEventKind.from(event.kind)
    val isMultiDay: Boolean get() = end != start
    val isFirstDay: Boolean get() = date == start
}
