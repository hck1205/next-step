package com.nextstep.app.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.common.asPercent
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.LabeledProgress
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.dialog.AddTreeGoalDialog
import com.nextstep.app.ui.components.dialog.TaskEditDialog
import com.nextstep.app.ui.components.row.HistoryEventRow
import com.nextstep.app.ui.goal.components.ChainCard
import com.nextstep.app.ui.goal.components.EditGoalDialog
import com.nextstep.app.ui.goal.components.GoalHeaderCard
import com.nextstep.app.ui.goal.components.GoalRewardCard
import com.nextstep.app.ui.components.dialog.PromiseRewardDialog
import com.nextstep.app.domain.reward.RewardStatus
import com.nextstep.app.ui.goal.components.LinkGoalDialog
import com.nextstep.app.ui.goal.components.SubTaskRow

/**
 * 목표 한 개. 위에서 아래로: 목표와 달성률 → 이루면 이어지는 목표 → 세부 할 일(누가 줬는지 · 마감 · 끝낸 날) → 작은 목표 → 기록.
 * 학생·학부모·멘토 누구나 세부 할 일을 줄 수 있고, 체크는 학생(어린 단계는 학부모도, 자기주도 사다리).
 */
@Composable
fun GoalScreen(caps: Capabilities, actions: GoalActions, viewModel: GoalViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GoalContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalContent(state: GoalUiState, caps: Capabilities, actions: GoalActions, onEvent: (GoalEvent) -> Unit) {
    var addingTask by remember { mutableStateOf(false) }
    var addingChild by remember { mutableStateOf(false) }
    var addingNext by remember { mutableStateOf(false) }
    var linking by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var promising by remember { mutableStateOf(false) }
    val node = state.node
    val canCheck = caps.canCheckTask(state.stage)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(node?.goal?.title ?: "목표", maxLines = 1) },
                navigationIcon = { IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (node == null) {
                if (state.loaded) item { AppCard { EmptyState("이 목표를 찾을 수 없어요") } }
                return@LazyColumn
            }
            item {
                GoalHeaderCard(
                    node, canClose = caps.canCloseGoals, onAchieve = { onEvent(GoalEvent.Achieve) }, onReopen = { onEvent(GoalEvent.Reopen) },
                    onArchive = { onEvent(GoalEvent.Archive) }, onEdit = { editing = true },
                )
            }
            if (state.reward != null || caps.canGiveRewards) item {
                GoalRewardCard(
                    state.reward, canGive = caps.canGiveRewards, achieved = node.isAchieved, onPromise = { promising = true },
                    onGive = { state.reward?.let { onEvent(GoalEvent.GiveReward(it.reward.id)) } }, onCancel = { state.reward?.let { onEvent(GoalEvent.CancelReward(it.reward.id)) } },
                )
            }
            item { ChainCard(state.chain, node.isAchieved, onOpen = actions.onOpenGoal, onChange = if (caps.canAssignTasks) ({ linking = true }) else null) }
            if (node.isAchieved && caps.canAssignTasks) {
                item {
                    AppCard {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("달성했어요! 다음 목표로 이어 갈까요?", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            TextButton(onClick = { addingNext = true }) { Text(node.parent?.let { "\"${it.title}\"로 이어지는 다음 목표 만들기" } ?: "다음 목표 만들기") }
                        }
                    }
                }
            }
            item {
                SectionTitle(
                    "세부 할 일 · ${node.doneTasks}/${node.totalTasks}",
                    action = if (caps.canAssignTasks && !node.isAchieved) ({ TextButton(onClick = { addingTask = true }) { Text(if (caps.isStudent) "추가" else "할 일 주기") } }) else null,
                )
            }
            if (node.tasks.isEmpty()) item { AppCard { EmptyState("세부 할 일이 없어요. 작게 나눠 넣으면 달성률이 보여요.") } }
            else item {
                AppCard {
                    Column {
                        node.tasks.forEach { t ->
                            SubTaskRow(
                                t, state.subjects, state.today, canCheck = canCheck, onToggle = { onEvent(GoalEvent.ToggleTask(t.id, !t.done)) },
                                onDelete = if (caps.canAssignTasks && !t.done) ({ onEvent(GoalEvent.DeleteTask(t.id)) }) else null,
                            )
                        }
                    }
                }
            }
            item {
                SectionTitle(
                    "이 목표로 이어지는 작은 목표 · ${node.achievedChildren}/${node.children.size}",
                    action = if (caps.canAssignTasks && !node.isAchieved) ({ TextButton(onClick = { addingChild = true }) { Text("작은 목표 추가") } }) else null,
                )
            }
            items(state.children, key = { "c-" + it.goal.id }) { c ->
                AppCard(onClick = { actions.onOpenGoal(c.goal.id) }) {
                    Column {
                        Text((if (c.isAchieved) "✓ " else "") + c.goal.title, style = MaterialTheme.typography.bodyLarge)
                        LabeledProgress(label = "${c.rate.asPercent()} · 할 일 ${c.doneTasks}/${c.totalTasks}", ratio = c.rate, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            if (state.history.isNotEmpty()) {
                item { SectionTitle("기록") }
                item { AppCard { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { state.history.forEach { HistoryEventRow(it) } } } }
            }
        }
    }
    val goal = node?.goal ?: return
    val area = GoalArea.from(goal.area)
    if (addingTask) {
        TaskEditDialog(existing = null, subjects = state.subjects, defaultDate = state.today.plusDays(DEFAULT_DUE_DAYS), onDismiss = { addingTask = false }) { title, subjectId, type, due ->
            onEvent(GoalEvent.AddTask(title, subjectId, type, due, caps.actingRoleName))
        }
    }
    if (addingChild) {
        AddTreeGoalDialog(
            targets = emptyList(), today = state.today, fixedParent = goal, initialArea = area, onDismiss = { addingChild = false },
            onSave = { title, why, a, target, _ -> onEvent(GoalEvent.AddChild(title, why, a, target, caps.actingRoleName)) },
        )
    }
    if (addingNext) {
        AddTreeGoalDialog(
            targets = emptyList(), today = state.today, fixedParent = node.parent, initialArea = area, onDismiss = { addingNext = false },
            onSave = { title, why, a, target, _ -> onEvent(GoalEvent.AddNext(title, why, a, target, caps.actingRoleName)) },
        )
    }
    if (linking) LinkGoalDialog(state.linkTargets, goal.leadsTo, onDismiss = { linking = false }, onSave = { onEvent(GoalEvent.Link(it)) })
    if (promising) {
        PromiseRewardDialog(
            goals = emptyList(), levels = emptyList(), fixedGoal = goal, initialTitle = state.reward?.takeIf { it.status == RewardStatus.PROMISED }?.reward?.title.orEmpty(),
            onDismiss = { promising = false }, onSave = { _, _, title -> onEvent(GoalEvent.PromiseReward(title)) },
        )
    }
    if (editing) EditGoalDialog(goal, state.today, onDismiss = { editing = false }, onSave = { t, w, d -> onEvent(GoalEvent.Edit(t, w, d)) })
}

private const val DEFAULT_DUE_DAYS = 3L
