package com.nextstep.app.ui.parent

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.model.TaskType
import java.time.LocalDate

/** ParentDashboard 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface ParentDashboardEvent {
    data class AddNote(val text: String) : ParentDashboardEvent
    data class DeleteNote(val id: String) : ParentDashboardEvent
    data class AssignTask(val title: String, val subjectId: String?, val type: TaskType, val due: LocalDate, val createdByRole: String) : ParentDashboardEvent
}
