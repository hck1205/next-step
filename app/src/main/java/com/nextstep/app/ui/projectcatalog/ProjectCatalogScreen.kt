package com.nextstep.app.ui.projectcatalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.project.ProjectCategory
import com.nextstep.app.domain.project.ProjectPlan
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.input.SegmentedRow
import com.nextstep.app.ui.projectcatalog.components.PlanCard
import com.nextstep.app.ui.projectcatalog.components.StartProjectDialog

/**
 * 기록 › 교육 프로젝트 › 새로 시작. "언제까지 무엇을 할 수 있게"를 단계로 나눈 계획을 분류별로 고릅니다.
 * 시작할 때 아이 나이에 맞는 단계가 미리 골라져 있고, 이미 할 수 있으면 더 뒤 단계부터 시작합니다.
 */
@Composable
fun ProjectCatalogScreen(caps: Capabilities, actions: ProjectCatalogActions, viewModel: ProjectCatalogViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProjectCatalogContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@Composable
internal fun ProjectCatalogContent(state: ProjectCatalogUiState, caps: Capabilities, actions: ProjectCatalogActions, onEvent: (ProjectCatalogEvent) -> Unit) {
    var starting by remember { mutableStateOf<ProjectPlan?>(null) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            AppCard {
                Text(
                    (state.ageLabel?.let { "$it 기준으로 지금 할 수 있는 것부터 보여요. " } ?: "") +
                        "단계마다 하루 루틴(무엇을·몇 분·주 며칠)과 통과 기준이 있고, 양은 단계마다 조금씩 늘어요.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            SegmentedRow(
                options = listOf<ProjectCategory?>(null) + state.categories, selected = state.filter,
                label = { it?.label ?: "전체" }, onSelect = { onEvent(ProjectCatalogEvent.SelectCategory(it)) }, modifier = Modifier.fillMaxWidth(),
            )
        }
        items(state.shown, key = { it.id }) { plan ->
            PlanCard(
                plan = plan, suggestedIndex = state.suggested[plan.id] ?: 0, started = plan.id in state.started,
                onStart = if (caps.canManageGoals && plan.id !in state.started) ({ starting = plan }) else null,
            )
        }
    }
    starting?.let { plan ->
        StartProjectDialog(
            plan = plan, suggestedIndex = state.suggested[plan.id] ?: 0, today = state.today,
            onDismiss = { starting = null },
            onStart = { index ->
                onEvent(ProjectCatalogEvent.Start(plan.id, index, caps.actingRoleName))
                starting = null
                actions.onStarted()
            },
        )
    }
}
