package com.nextstep.app.ui.projects

import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.project.RoutineItem

/** 진행 중 화면의 사용자 의도. */
sealed interface ProjectsEvent {
    data class SelectCategory(val category: ProjectCategory?) : ProjectsEvent
    /** 오늘 루틴 한 줄 체크(다시 누르면 취소). */
    data class ToggleRoutine(val progress: ProjectProgress, val item: RoutineItem) : ProjectsEvent
}
