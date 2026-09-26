package com.nextstep.app.ui.parent

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.project.RoutineItem
import java.time.LocalDate

/** ParentDashboard 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface ParentDashboardEvent {
    data class AssignTask(val title: String, val subjectId: String?, val type: TaskType, val due: LocalDate, val createdByRole: String) : ParentDashboardEvent
    /** 오늘의 루틴 한 줄 체크(다시 누르면 취소). 어린 아이의 루틴은 부모가 함께 하고 체크합니다. */
    data class ToggleRoutine(val progress: ProjectProgress, val item: RoutineItem) : ParentDashboardEvent
}
