package com.nextstep.app.ui.goaltree

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 기록 › 목표·할 일 › 목표. 목표 트리를 계산하고 새 목표를 만듭니다(학생·학부모·멘토 누구나). */
class GoalTreeViewModel(
    streams: FamilyDataStreams,
    private val goals: GoalRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val filter = MutableStateFlow(GoalFilter.ACTIVE)
    private val area = MutableStateFlow<GoalArea?>(null)

    val state: StateFlow<GoalTreeUiState> = combine(streams.goals, streams.tasks, filter, area) { all, tasks, f, a ->
        GoalTreeUiState(loaded = true, nodes = GoalTree.nodes(all, tasks, today()), filter = f, area = a)
    }.asUiState(viewModelScope, GoalTreeUiState())

    fun onEvent(event: GoalTreeEvent) {
        when (event) {
            is GoalTreeEvent.SetFilter -> filter.value = event.filter
            is GoalTreeEvent.SetArea -> area.value = event.area
            is GoalTreeEvent.Create -> viewModelScope.launch {
                if (event.title.isBlank()) return@launch
                goals.add(GoalTree.create(event.title, event.why, event.area, event.target, event.leadsTo, event.createdByRole), emptyList())
            }
        }
    }
}
