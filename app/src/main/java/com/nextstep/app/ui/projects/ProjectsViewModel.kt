package com.nextstep.app.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.ProjectRepository
import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.domain.project.ProjectKind
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** 기록 › 교육 프로젝트 › 진행 중: 분류·종류(누가 줬는지)별로 나눈 프로젝트와 오늘 루틴 체크. 보이는 범위는 ProjectScopedStreams 가 정합니다. */
class ProjectsViewModel(
    streams: FamilyDataStreams,
    private val projects: ProjectRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val filter = MutableStateFlow<ProjectCategory?>(null)
    private val kind = MutableStateFlow<ProjectKind?>(null)

    val state: StateFlow<ProjectsUiState> = combine(streams.goals, streams.goalSteps, streams.projectLogs, filter, kind) { goals, steps, logs, f, k ->
        val all = ProjectPlanner.progressAll(goals, steps, logs, today())
        val categories = ProjectCategory.entries.filter { c -> all.any { it.plan.category == c } }
        val kinds = ProjectKind.entries.filter { c -> all.any { it.kind == c } }
        ProjectsUiState(loaded = true, all = all, categories = categories, filter = f?.takeIf { it in categories }, kinds = kinds, kind = k?.takeIf { it in kinds })
    }.asUiState(viewModelScope, ProjectsUiState())

    fun onEvent(event: ProjectsEvent) {
        when (event) {
            is ProjectsEvent.SelectCategory -> filter.value = event.category
            is ProjectsEvent.SelectKind -> kind.value = event.kind
            is ProjectsEvent.ToggleRoutine -> viewModelScope.launch { projects.toggleRoutine(event.progress, event.item, today()) }
        }
    }
}
