package com.nextstep.app.ui.calendar

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.TaskType
import java.time.LocalDate
import java.time.LocalTime

/** Calendar 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface CalendarEvent {
    data object PrevMonth : CalendarEvent
    data object NextMonth : CalendarEvent
    data class Select(val date: LocalDate) : CalendarEvent
    data object Today : CalendarEvent
    data class SaveEvent(val existing: EventEntity?, val title: String, val subjectId: String?, val type: EventType, val date: LocalDate, val start: LocalTime, val end: LocalTime, val repeatWeekly: Boolean, val location: String, val memo: String) : CalendarEvent
    data class DeleteEvent(val id: String) : CalendarEvent
    data class SaveTask(val existing: TaskEntity?, val title: String, val subjectId: String?, val type: TaskType, val due: LocalDate, val role: String) : CalendarEvent
    data class ToggleTask(val task: TaskEntity) : CalendarEvent
    data class DeleteTask(val id: String) : CalendarEvent
}
