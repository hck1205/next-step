package com.nextstep.app.ui.mentor

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.model.TaskType
import java.time.LocalDate

/** MentorDashboard 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface MentorDashboardEvent {
    data class SetSubjects(val ids: List<String>) : MentorDashboardEvent
    data class AssignTask(val title: String, val subjectId: String?, val type: TaskType, val due: LocalDate) : MentorDashboardEvent
    data class DeleteTask(val id: String) : MentorDashboardEvent
}
