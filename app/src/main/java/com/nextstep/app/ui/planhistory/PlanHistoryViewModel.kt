package com.nextstep.app.ui.planhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.goaltree.PlanHistory
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** 기록 › 목표·할 일 › 기록. 읽기만 합니다. */
class PlanHistoryViewModel(
    streams: FamilyDataStreams,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {

    val state: StateFlow<PlanHistoryUiState> = combine(streams.tasks, streams.goals, streams.subjects) { tasks, goals, subjects ->
        val day = today()
        val nodes = GoalTree.nodes(goals, tasks, day)
        PlanHistoryUiState(
            loaded = true, recentRate = PlanHistory.recentRate(tasks, day), weeks = PlanHistory.weeks(tasks, day),
            byAssigner = PlanHistory.byAssigner(tasks, day), bySubject = PlanHistory.bySubject(tasks, subjects, day),
            achieved = nodes.filter { it.goal.status == GoalStatus.DONE }.sortedByDescending { it.goal.doneAt ?: 0L }.map { it to GoalTree.nextStepLine(it, nodes) },
            timeline = PlanHistory.timeline(goals, tasks),
        )
    }.asUiState(viewModelScope, PlanHistoryUiState())
}
