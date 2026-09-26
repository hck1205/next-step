package com.nextstep.app.ui.planhistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.goaltree.RateBy
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.LabeledProgress
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.row.HistoryEventRow
import com.nextstep.app.ui.planhistory.components.WeekBars

/**
 * 기록 › 목표·할 일 › 기록. 할 일과 목표가 쌓인 흔적: 달성률(최근 4주 · 주별 · 누가 준 일 · 과목별), 달성한 목표가 이어진 목표, 시간순 기록.
 * 비교는 아이 자신의 지난 기록과만 합니다.
 */
@Composable
fun PlanHistoryScreen(actions: PlanHistoryActions, viewModel: PlanHistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PlanHistoryContent(state = state, actions = actions)
}

@Composable
internal fun PlanHistoryContent(state: PlanHistoryUiState, actions: PlanHistoryActions) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("최근 4주 달성률", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(state.recentRate?.let { "${(it * PERCENT).toInt()}%" } ?: "마감이던 할 일이 없어요", style = MaterialTheme.typography.headlineSmall)
                    Text("마감이 지난(오늘까지) 할 일 중 끝낸 비율이에요", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    WeekBars(state.weeks)
                }
            }
        }
        if (state.byAssigner.isNotEmpty()) {
            item { SectionTitle("누가 준 할 일") }
            item { RateCard(state.byAssigner) }
        }
        if (state.bySubject.isNotEmpty()) {
            item { SectionTitle("과목별") }
            item { RateCard(state.bySubject) }
        }
        item { SectionTitle("달성한 목표 · ${state.achieved.size}") }
        if (state.achieved.isEmpty()) item { AppCard { EmptyState("아직 달성한 목표가 없어요") } }
        items(state.achieved, key = { "a-" + it.first.goal.id }) { (node, next) ->
            AppCard(onClick = { actions.onOpenGoal(node.goal.id) }) {
                Column {
                    Text(node.goal.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        listOfNotNull(node.goal.doneAt?.let { "${DateUtils.formatDate(DateUtils.toLocalDate(it))} 달성" }, "할 일 ${node.totalTasks}개", next).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (state.timeline.isNotEmpty()) {
            item { SectionTitle("지난 기록") }
            item { AppCard { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { state.timeline.forEach { HistoryEventRow(it) } } } }
        }
    }
}

@Composable
private fun RateCard(rows: List<RateBy>) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEach { r -> LabeledProgress(label = "${r.label} · ${r.done}/${r.total}", ratio = r.rate, color = MaterialTheme.colorScheme.secondary, trailing = "${(r.rate * PERCENT).toInt()}%") }
        }
    }
}

private const val PERCENT = 100
