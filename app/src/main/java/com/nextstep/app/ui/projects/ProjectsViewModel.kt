package com.nextstep.app.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.ProjectRepository
import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.project.RoutineItem
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 기록 › 교육 프로젝트 › 진행 중: 분류별로 나눈 프로젝트와 오늘 루틴 체크. */
class ProjectsViewModel(
    streams: FamilyDataStreams,
    private val projects: ProjectRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val filter = MutableStateFlow<ProjectCategory?>(null)

    val state: StateFlow<ProjectsUiState> = combine(streams.goals, streams.goalSteps, streams.projectLogs, filter) { goals, steps, logs, f ->
        val all = ProjectPlanner.progressAll(goals, steps, logs, today())
        val categories = ProjectCategory.entries.filter { c -> all.any { it.plan.category == c } }
        ProjectsUiState(loaded = true, all = all, categories = categories, filter = f?.takeIf { it in categories })
    }.asUiState(viewModelScope, ProjectsUiState())

    fun onEvent(event: ProjectsEvent) {
        when (event) {
            is ProjectsEvent.SelectCategory -> filter.value = event.category
            is ProjectsEvent.ToggleRoutine -> toggle(event.progress, event.item)
        }
    }

    private fun toggle(progress: ProjectProgress, item: RoutineItem) = viewModelScope.launch {
        val phase = progress.current ?: return@launch
        projects.toggle(progress.goalId, phase.key, item.name, item.minutes, today().toEpochDay())
    }
}
