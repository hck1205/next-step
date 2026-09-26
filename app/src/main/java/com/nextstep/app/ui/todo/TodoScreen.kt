package com.nextstep.app.ui.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.StatTile
import com.nextstep.app.ui.components.input.SegmentedRow
import com.nextstep.app.ui.todo.components.LaneCard
import androidx.compose.foundation.layout.Row

/**
 * 기록 › 목표·할 일 › 할 일. 모든 할 일(내가 정한 것 · 부모·멘토가 준 것 · 목표의 세부 할 일)을 과목별로 정리하고,
 * 시스템 추천(시험 대비 · 이해도 낮은 단원 · 로드맵 · 수업 뒤 복습 · 다음 예습)을 그 과목 아래에 급한 순서로 붙입니다.
 */
@Composable
fun TodoScreen(caps: Capabilities, actions: TodoActions, viewModel: TodoViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TodoContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@Composable
internal fun TodoContent(state: TodoUiState, caps: Capabilities, actions: TodoActions, onEvent: (TodoEvent) -> Unit) {
    val canCheck = caps.canCheckTask(state.stage)
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("밀린 것", "${state.overdueCount}", modifier = Modifier.weight(1f))
                StatTile("오늘", "${state.todayCount}", modifier = Modifier.weight(1f))
                StatTile("이번 주 끝", "${state.doneThisWeek}", modifier = Modifier.weight(1f))
                StatTile("추천", "${state.suggestionCount}", modifier = Modifier.weight(1f))
            }
        }
        item {
            SegmentedRow(options = TodoFilter.entries, selected = state.filter, label = { it.label }, onSelect = { onEvent(TodoEvent.SetFilter(it)) }, modifier = Modifier.fillMaxWidth())
        }
        if (state.loaded && state.shown.isEmpty()) item { AppCard { EmptyState(if (state.filter == TodoFilter.ALL) "할 일과 추천이 없어요. 목표에서 세부 할 일을 주거나 + 로 할 일을 만들어요." else "${state.filter.label}이 없어요") } }
        items(state.shown, key = { "lane-" + (it.subject?.id ?: "other") }) { lane ->
            LaneCard(
                lane = lane, goalTitle = state::goalTitle, goals = state.goals, canCheck = canCheck, canAccept = caps.canAssignTasks,
                onToggle = { t -> onEvent(TodoEvent.Toggle(t.id, !t.done)) },
                onAccept = { s, goalId -> onEvent(TodoEvent.Accept(s, goalId, caps.actingRoleName)) },
                onOpenGoal = actions.onOpenGoal, onOpenSubject = actions.onOpenSubject,
            )
        }
    }
}
