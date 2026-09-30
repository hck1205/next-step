package com.nextstep.app.ui.goal.components

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.reward.RewardKind
import com.nextstep.app.domain.reward.RewardStatus
import com.nextstep.app.domain.reward.RewardTarget
import com.nextstep.app.ui.components.dialog.AddTreeGoalDialog
import com.nextstep.app.ui.components.dialog.PromiseRewardDialog
import com.nextstep.app.ui.components.dialog.TaskEditDialog
import com.nextstep.app.ui.goal.GoalEvent
import com.nextstep.app.ui.goal.GoalUiState

/** 열린 창([dialog]) 하나를 그립니다. 목표를 찾지 못했으면 아무것도 열지 않습니다. */
@Composable
internal fun GoalDialogs(dialog: GoalDialog?, state: GoalUiState, onEvent: (GoalEvent) -> Unit, onDismiss: () -> Unit) {
    val node = state.node ?: return
    val goal = node.goal
    val area = GoalArea.from(goal.area)
    when (dialog) {
        null -> Unit
        GoalDialog.ADD_TASK -> TaskEditDialog(existing = null, subjects = state.subjects, defaultDate = state.today.plusDays(DEFAULT_DUE_DAYS), onDismiss = onDismiss) { title, subjectId, type, due ->
            onEvent(GoalEvent.AddTask(title, subjectId, type, due))
        }
        GoalDialog.ADD_CHILD -> AddTreeGoalDialog(
            targets = emptyList(), today = state.today, fixedParent = goal, initialArea = area, onDismiss = onDismiss,
            onSave = { title, why, a, target, _ -> onEvent(GoalEvent.AddChild(title, why, a, target)) },
        )
        GoalDialog.ADD_NEXT -> AddTreeGoalDialog(
            targets = emptyList(), today = state.today, fixedParent = node.parent, initialArea = area, onDismiss = onDismiss,
            onSave = { title, why, a, target, _ -> onEvent(GoalEvent.AddNext(title, why, a, target)) },
        )
        GoalDialog.LINK -> LinkGoalDialog(state.linkTargets, goal.leadsTo, onDismiss = onDismiss, onSave = { onEvent(GoalEvent.Link(it)) })
        GoalDialog.PROMISE -> PromiseRewardDialog(
            targets = listOf(RewardTarget(RewardKind.GOAL, goal.id, goal.title)), ideas = state.rewardIdeas, hint = state.rewardHint,
            initialTitle = state.reward?.takeIf { it.status == RewardStatus.PROMISED }?.reward?.title.orEmpty(),
            onDismiss = onDismiss, onSave = { _, title -> onEvent(GoalEvent.PromiseReward(title)) },
        )
        GoalDialog.EDIT -> EditGoalDialog(goal, state.today, onDismiss = onDismiss, onSave = { t, w, d -> onEvent(GoalEvent.Edit(t, w, d)) })
    }
}

private const val DEFAULT_DUE_DAYS = 3L
