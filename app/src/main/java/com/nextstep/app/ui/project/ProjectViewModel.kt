package com.nextstep.app.ui.project

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.data.repository.ProjectRepository
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.project.RoutineItem
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 교육 프로젝트 한 개: 목표·속도·도착 예상, 오늘 루틴, 통과 기준, 단계 일정. 목표 id 는 내비게이션 인자 "goalId". */
class ProjectViewModel(
    savedStateHandle: SavedStateHandle,
    private val streams: FamilyDataStreams,
    private val goals: GoalRepository,
    private val projects: ProjectRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val goalId: String = checkNotNull(savedStateHandle.get<String>("goalId"))

    val state: StateFlow<ProjectUiState> = combine(streams.goals, streams.goalSteps, streams.projectLogs) { all, steps, logs ->
        val day = today()
        val goal = all.firstOrNull { it.id == goalId && !it.deleted }
        val plan = goal?.let { ProjectPlanner.planOf(it) }
        if (goal == null || plan == null) return@combine ProjectUiState(loaded = true, today = day)
        val mine = steps.filter { it.goalId == goalId && !it.deleted }
        ProjectUiState(
            loaded = true, progress = ProjectPlanner.progress(plan, goal, mine, logs, day), slots = ProjectPlanner.slotsOf(plan, goal, mine),
            skipped = mine.count { it.status == MilestoneStatus.SKIPPED }, today = day,
        )
    }.asUiState(viewModelScope, ProjectUiState())

    fun onEvent(event: ProjectEvent) {
        when (event) {
            is ProjectEvent.ToggleRoutine -> toggle(event.item)
            ProjectEvent.PassCheckpoint -> passCheckpoint()
            ProjectEvent.Archive -> viewModelScope.launch { goals.setGoalStatus(goalId, GoalStatus.ARCHIVED) }
        }
    }

    private fun toggle(item: RoutineItem) = viewModelScope.launch {
        val phase = state.value.progress?.current ?: return@launch
        projects.toggle(goalId, phase.key, item.name, item.minutes, today().toEpochDay())
    }

    /** 지금 단계를 통과로, 다음 단계를 진행 중으로. 다음이 없으면 프로젝트를 달성으로 닫습니다. */
    private fun passCheckpoint() = viewModelScope.launch {
        val p = state.value.progress ?: return@launch
        val current = p.current ?: return@launch
        val steps = streams.goalSteps.first().filter { it.goalId == goalId && !it.deleted }
        val byKey = steps.associateBy { ProjectPlanner.phaseKeyOf(it) }
        byKey[current.key]?.let { goals.setStepStatus(it.id, MilestoneStatus.DONE) }
        val next = p.next
        if (next == null) goals.setGoalStatus(goalId, GoalStatus.DONE)
        else byKey[next.key]?.let { goals.setStepStatus(it.id, MilestoneStatus.IN_PROGRESS) }
    }
}
