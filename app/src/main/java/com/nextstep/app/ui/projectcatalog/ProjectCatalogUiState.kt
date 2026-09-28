package com.nextstep.app.ui.projectcatalog

import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.domain.project.ProjectKind
import com.nextstep.app.domain.project.ProjectPlan
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/**
 * 기록 › 교육 프로젝트 › 새로 시작. [plans] 는 아이 나이에 맞는 순서(지금 할 수 있는 것 → 곧 시작할 것),
 * [suggested] 는 프로젝트마다 나이에 맞는 시작 단계, [started] 는 이미 진행 중인 프로젝트 id,
 * [startKind] 는 지금 사람이 시작하면 생길 종류(누가 준 프로젝트인지 · 누가 보는지)입니다.
 */
data class ProjectCatalogUiState(
    val loaded: Boolean = false,
    val plans: List<ProjectPlan> = emptyList(),
    val suggested: Map<String, Int> = emptyMap(),
    val started: Set<String> = emptySet(),
    val filter: ProjectCategory? = null,
    val ageLabel: String? = null,
    val today: LocalDate = DateUtils.today(),
    val startKind: ProjectKind = ProjectKind.SELF,
) {
    val categories: List<ProjectCategory> by lazy { ProjectCategory.entries.filter { c -> plans.any { it.category == c } } }
    val shown: List<ProjectPlan> by lazy { plans.filter { filter == null || it.category == filter } }
}
