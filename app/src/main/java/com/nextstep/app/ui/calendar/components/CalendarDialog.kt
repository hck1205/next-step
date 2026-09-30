package com.nextstep.app.ui.calendar.components

import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.TaskEntity

/** 달력에서 한 번에 하나만 열리는 창. [event]·[task] 가 null 이면 새로 만들기. */
internal sealed interface CalendarDialog {
    data class EditEvent(val event: EventEntity?) : CalendarDialog
    data class EditTask(val task: TaskEntity?) : CalendarDialog
}
