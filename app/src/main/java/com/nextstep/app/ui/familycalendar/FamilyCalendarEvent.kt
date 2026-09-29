package com.nextstep.app.ui.familycalendar

import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.domain.entry.FamilyEventDraft
import java.time.LocalDate

/** 가족 달력 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface FamilyCalendarEvent {
    data object PrevMonth : FamilyCalendarEvent
    data object NextMonth : FamilyCalendarEvent
    data object Today : FamilyCalendarEvent
    data class Select(val date: LocalDate) : FamilyCalendarEvent
    /** 한 사람의 일정만(가족 모두의 일정 포함). null 이면 모두. */
    data class Filter(val memberId: String?) : FamilyCalendarEvent
    data class Save(val existing: FamilyEventEntity?, val draft: FamilyEventDraft) : FamilyCalendarEvent
    data class Delete(val id: String) : FamilyCalendarEvent
}
