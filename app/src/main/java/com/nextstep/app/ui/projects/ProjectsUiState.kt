package com.nextstep.app.ui.projects

import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.domain.project.ProjectProgress

/** 기록 › 교육 프로젝트 › 진행 중. [categories] 는 진행 중인 프로젝트가 있는 분류만(칩 줄). [filter] 가 null 이면 전체. */
data class ProjectsUiState(
    val loaded: Boolean = false,
    val all: List<ProjectProgress> = emptyList(),
    val categories: List<ProjectCategory> = emptyList(),
    val filter: ProjectCategory? = null,
) {
    val shown: List<ProjectProgress> get() = all.filter { filter == null || it.plan.category == filter }
}
