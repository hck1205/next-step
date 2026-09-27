package com.nextstep.app.ui.calendar.components

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.calendar.CalendarEvent
import com.nextstep.app.ui.calendar.CalendarUiState
import com.nextstep.app.ui.components.dialog.EventEditDialog
import com.nextstep.app.ui.components.dialog.TaskEditDialog

/** 일정·할 일 편집 창. 고른 날짜가 기본 날짜입니다. */
@Composable
internal fun CalendarDialogs(dialog: CalendarDialog?, state: CalendarUiState, caps: Capabilities, onEvent: (CalendarEvent) -> Unit, onDismiss: () -> Unit) {
    when (dialog) {
        null -> Unit
        is CalendarDialog.EditEvent -> {
            val existing = dialog.event
            EventEditDialog(
                existing = existing, subjects = state.subjects, defaultDate = state.selected,
                onDismiss = onDismiss,
                onDelete = existing?.let { e -> { onEvent(CalendarEvent.DeleteEvent(e.id)) } },
            ) { title, subjectId, type, date, start, end, repeat, location, memo ->
                onEvent(CalendarEvent.SaveEvent(existing, title, subjectId, type, date, start, end, repeat, location, memo))
            }
        }
        is CalendarDialog.EditTask -> TaskEditDialog(existing = dialog.task, subjects = state.subjects, defaultDate = state.selected, onDismiss = onDismiss) { title, subjectId, type, due ->
            onEvent(CalendarEvent.SaveTask(dialog.task, title, subjectId, type, due, caps.actingRoleName))
        }
    }
}
