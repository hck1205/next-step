package com.nextstep.app.ui.goaltree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.dialog.AddTreeGoalDialog
import com.nextstep.app.ui.components.input.SegmentedRow
import com.nextstep.app.ui.goaltree.components.GoalNodeCard

/**
 * 기록 › 목표·할 일 › 목표. 학생·학부모·멘토 누구나 목표를 만들고, 큰 목표 아래 작은 목표를 이어 붙입니다.
 * 카드마다 달성률(세부 할 일 + 작은 목표), 기한, 밀린 할 일 · 멈춘 날수. 눌러서 세부 할 일을 주고 챙깁니다.
 */
@Composable
fun GoalTreeScreen(caps: Capabilities, actions: GoalTreeActions, viewModel: GoalTreeViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GoalTreeContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@Composable
internal fun GoalTreeContent(state: GoalTreeUiState, caps: Capabilities, actions: GoalTreeActions, onEvent: (GoalTreeEvent) -> Unit) {
    var adding by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SegmentedRow(
                    options = GoalFilter.entries, selected = state.filter, label = { "${it.label} ${state.count(it)}" },
                    onSelect = { onEvent(GoalTreeEvent.SetFilter(it)) }, modifier = Modifier.fillMaxWidth(),
                )
            }
            if (state.areas.size > 1) {
                item {
                    SegmentedRow(
                        options = listOf<GoalArea?>(null) + state.areas, selected = state.area, label = { it?.label ?: "전체" },
                        onSelect = { onEvent(GoalTreeEvent.SetArea(it)) }, modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (state.loaded && state.roots.isEmpty()) {
                item {
                    AppCard {
                        EmptyState(
                            if (state.filter == GoalFilter.ACTIVE) "아직 목표가 없어요. 큰 목표를 만들고, 그 아래 작은 목표와 세부 할 일을 이어 보세요."
                            else "${state.filter.label}한 목표가 없어요",
                        )
                    }
                }
            }
            state.roots.forEach { root -> tree(root.goal.id, state, 0, actions) }
        }
        if (caps.canAssignTasks) {
            ExtendedFloatingActionButton(
                onClick = { adding = true }, icon = { Icon(Icons.Default.Add, contentDescription = null) }, text = { Text("목표 만들기") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 96.dp),
            )
        }
    }
    if (adding) {
        AddTreeGoalDialog(
            targets = GoalTree.linkTargets(null, state.nodes.map { it.goal }), today = DateUtils.today(), onDismiss = { adding = false },
            onSave = { title, why, area, target, leadsTo -> onEvent(GoalTreeEvent.Create(title, why, area, target, leadsTo, caps.actingRoleName)) },
        )
    }
}

/** 큰 목표 → 작은 목표 순서로 들여 써서 그립니다(깊이 [MAX_DEPTH] 까지). */
private fun LazyListScope.tree(goalId: String, state: GoalTreeUiState, depth: Int, actions: GoalTreeActions) {
    val node = state.nodes.firstOrNull { it.goal.id == goalId } ?: return
    item(key = "g-$goalId-$depth") {
        GoalNodeCard(node, depth, onOpen = { actions.onOpenGoal(goalId) })
    }
    if (depth < MAX_DEPTH) state.childrenOf(goalId).forEach { tree(it.goal.id, state, depth + 1, actions) }
}

private const val MAX_DEPTH = 3
