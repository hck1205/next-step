package com.nextstep.app.ui.calendar

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.entry.EventDraft
import java.time.LocalDate

/** Calendar 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface CalendarEvent {
    data object PrevMonth : CalendarEvent
    data object NextMonth : CalendarEvent
    data class Select(val date: LocalDate) : CalendarEvent
    data object Today : CalendarEvent
    data class SaveEvent(val existing: EventEntity?, val draft: EventDraft) : CalendarEvent
    data class DeleteEvent(val id: String) : CalendarEvent
    data class SaveTask(val existing: TaskEntity?, val title: String, val subjectId: String?, val type: TaskType, val due: LocalDate) : CalendarEvent
    data class ToggleTask(val task: TaskEntity) : CalendarEvent
    data class DeleteTask(val id: String) : CalendarEvent
}
