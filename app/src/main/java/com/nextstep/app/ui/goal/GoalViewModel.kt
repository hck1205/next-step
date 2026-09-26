package com.nextstep.app.ui.goal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.goaltree.PlanHistory
import com.nextstep.app.domain.selfdirection.SelfDirection
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 목표 한 개(route "goal/{goalId}"): 세부 할 일 주기 · 체크 · 달성 · 이어지는 목표 바꾸기 · 작은 목표와 다음 목표 만들기. */
class GoalViewModel(
    savedStateHandle: SavedStateHandle,
    streams: FamilyDataStreams,
    private val goals: GoalRepository,
    private val tasks: TaskRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val goalId: String = checkNotNull(savedStateHandle.get<String>("goalId"))

    val state: StateFlow<GoalUiState> = combine(streams.goals, streams.tasks, streams.subjects, streams.members) { all, allTasks, subjects, members ->
        val day = today()
        val goal = all.firstOrNull { it.id == goalId && !it.deleted && GoalTree.isTreeGoal(it) }
            ?: return@combine GoalUiState(loaded = true, today = day)
        val nodes = GoalTree.nodes(all, allTasks, day)
        val node = nodes.first { it.goal.id == goalId }
        val family = GoalTree.descendants(goalId, all) + goalId
        GoalUiState(
            loaded = true, node = node,
            chain = node.chain.mapNotNull { c -> nodes.firstOrNull { it.goal.id == c.id } },
            children = nodes.filter { it.goal.leadsTo == goalId && it.goal.status != GoalStatus.ARCHIVED },
            linkTargets = GoalTree.linkTargets(goal, all),
            history = PlanHistory.timeline(all.filter { it.id in family }, allTasks.filter { it.goalId in family }),
            subjects = subjects, stage = SelfDirection.stageOf(StudentContext.of(members, day).student, day), today = day,
        )
    }.asUiState(viewModelScope, GoalUiState())

    fun onEvent(event: GoalEvent) {
        val goal = state.value.node?.goal
        when (event) {
            is GoalEvent.AddTask -> viewModelScope.launch {
                if (goal == null || event.title.isBlank()) return@launch
                tasks.save(GoalTree.subTask(goal, event.title, event.due, event.subjectId, event.type, event.createdByRole))
            }
            is GoalEvent.ToggleTask -> viewModelScope.launch { tasks.setDone(event.taskId, event.done) }
            is GoalEvent.DeleteTask -> viewModelScope.launch { tasks.delete(event.taskId) }
            GoalEvent.Achieve -> viewModelScope.launch { goals.setGoalStatus(goalId, GoalStatus.DONE) }
            GoalEvent.Reopen -> viewModelScope.launch { goals.setGoalStatus(goalId, GoalStatus.ACTIVE) }
            GoalEvent.Archive -> viewModelScope.launch { goals.setGoalStatus(goalId, GoalStatus.ARCHIVED) }
            is GoalEvent.Link -> viewModelScope.launch { goals.link(goalId, event.leadsTo) }
            is GoalEvent.Edit -> viewModelScope.launch { goals.edit(goalId, event.title, event.why, event.target?.toEpochDay()) }
            is GoalEvent.AddChild -> viewModelScope.launch {
                if (event.title.isBlank()) return@launch
                goals.add(GoalTree.create(event.title, event.why, event.area, event.target, goalId, event.createdByRole), emptyList())
            }
            is GoalEvent.AddNext -> viewModelScope.launch {
                if (event.title.isBlank()) return@launch
                goals.add(GoalTree.create(event.title, event.why, event.area, event.target, goal?.leadsTo, event.createdByRole), emptyList())
            }
        }
    }
}
