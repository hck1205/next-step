package com.nextstep.app.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.nextstep.app.domain.goaltree.GoalNode
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.layout.BackButton
import com.nextstep.app.ui.components.layout.ScreenPadding
import com.nextstep.app.ui.components.row.HistoryEventRow
import com.nextstep.app.ui.goal.components.ChainCard
import com.nextstep.app.ui.goal.components.ChildGoalCard
import com.nextstep.app.ui.goal.components.GoalDialog
import com.nextstep.app.ui.goal.components.GoalDialogs
import com.nextstep.app.ui.goal.components.GoalHeaderCard
import com.nextstep.app.ui.goal.components.GoalRewardCard
import com.nextstep.app.ui.goal.components.NextGoalCard
import com.nextstep.app.ui.goal.components.SubTaskListCard

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
    var dialog by remember { mutableStateOf<GoalDialog?>(null) }
    val open: (GoalDialog) -> Unit = { dialog = it }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.node?.goal?.title ?: "목표", maxLines = 1) },
                navigationIcon = { BackButton(actions.onBack) },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenPadding.detail,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val node = state.node
            if (node == null) {
                if (state.loaded) item { EmptyCard("이 목표를 찾을 수 없어요") }
            } else {
                goalTop(node, state, caps, actions, onEvent, open)
                goalTasks(node, state, caps, onEvent, open)
                goalChildrenAndHistory(node, state, caps, actions, open)
            }
        }
    }
    GoalDialogs(dialog, state, caps, onEvent, onDismiss = { dialog = null })
}

/** 목표와 달성률 · 보상 · 이어지는 목표 길 · (이뤘으면) 다음 목표 권하기. */
private fun LazyListScope.goalTop(node: GoalNode, state: GoalUiState, caps: Capabilities, actions: GoalActions, onEvent: (GoalEvent) -> Unit, open: (GoalDialog) -> Unit) {
    item {
        GoalHeaderCard(
            node, canClose = caps.canCloseGoals, onAchieve = { onEvent(GoalEvent.Achieve) }, onReopen = { onEvent(GoalEvent.Reopen) },
            onArchive = { onEvent(GoalEvent.Archive) }, onEdit = { open(GoalDialog.EDIT) },
        )
    }
    // 보상은 가족의 일: 학부모가 약속·주고, 학생은 보기만, 멘토에게는 보이지 않음
    if (caps.isFamily && (state.reward != null || caps.canGiveRewards)) item {
        GoalRewardCard(
            state.reward, canGive = caps.canGiveRewards, achieved = node.isAchieved, onPromise = { open(GoalDialog.PROMISE) },
            onGive = { state.reward?.let { onEvent(GoalEvent.GiveReward(it.reward.id)) } }, onCancel = { state.reward?.let { onEvent(GoalEvent.CancelReward(it.reward.id)) } },
        )
    }
    item { ChainCard(state.chain, node.isAchieved, onOpen = actions.onOpenGoal, onChange = if (caps.canCreateTasks) ({ open(GoalDialog.LINK) }) else null) }
    if (node.isAchieved && caps.canCreateTasks) item { NextGoalCard(node.parent?.title, onAdd = { open(GoalDialog.ADD_NEXT) }) }
}

/** 세부 할 일: 제목 줄(추가 버튼) + 목록. */
private fun LazyListScope.goalTasks(node: GoalNode, state: GoalUiState, caps: Capabilities, onEvent: (GoalEvent) -> Unit, open: (GoalDialog) -> Unit) {
    val canAdd = caps.canCreateTasks && !node.isAchieved
    item {
        SectionTitle(
            "세부 할 일 · ${node.doneTasks}/${node.totalTasks}",
            action = if (canAdd) ({ TextButton(onClick = { open(GoalDialog.ADD_TASK) }) { Text(if (caps.isStudent) "추가" else "할 일 주기") } }) else null,
        )
    }
    item {
        SubTaskListCard(
            node.tasks, state.subjects, state.today, canCheck = caps.canCheckTask(state.stage), canDelete = caps.canCreateTasks,
            onToggle = { onEvent(GoalEvent.ToggleTask(it.id, !it.done)) }, onDelete = { onEvent(GoalEvent.DeleteTask(it.id)) },
        )
    }
}

/** 이 목표로 이어지는 작은 목표들과 이 목표의 기록. */
private fun LazyListScope.goalChildrenAndHistory(node: GoalNode, state: GoalUiState, caps: Capabilities, actions: GoalActions, open: (GoalDialog) -> Unit) {
    item {
        SectionTitle(
            "이 목표로 이어지는 작은 목표 · ${node.achievedChildren}/${node.children.size}",
            action = if (caps.canCreateTasks && !node.isAchieved) ({ TextButton(onClick = { open(GoalDialog.ADD_CHILD) }) { Text("작은 목표 추가") } }) else null,
        )
    }
    items(state.children, key = { "c-" + it.goal.id }) { c -> ChildGoalCard(c, onOpen = { actions.onOpenGoal(c.goal.id) }) }
    if (state.history.isNotEmpty()) {
        item { SectionTitle("기록") }
        item { AppCard { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { state.history.forEach { HistoryEventRow(it) } } } }
    }
}
