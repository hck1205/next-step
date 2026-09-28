package com.nextstep.app.ui.projects

import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.domain.project.ProjectKind
import com.nextstep.app.domain.project.ProjectProgress

/**
 * 기록 › 교육 프로젝트 › 진행 중. [categories] 는 진행 중인 프로젝트가 있는 분류만(칩 줄). [filter] 가 null 이면 전체.
 * [kinds] 는 보이는 프로젝트의 종류(스스로 · 학부모가 준 · 멘토가 준). 학생만 둘 이상을 봅니다(만든 사람과 학생만 봄).
 */
data class ProjectsUiState(
    val loaded: Boolean = false,
    val all: List<ProjectProgress> = emptyList(),
    val categories: List<ProjectCategory> = emptyList(),
    val filter: ProjectCategory? = null,
    val kinds: List<ProjectKind> = emptyList(),
    val kind: ProjectKind? = null,
) {
    val shown: List<ProjectProgress> by lazy { all.filter { (filter == null || it.plan.category == filter) && (kind == null || it.kind == kind) } }
}
