package com.nextstep.app.ui.goals.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.row.GoalStepRow
import com.nextstep.app.ui.components.card.LabeledProgress
import com.nextstep.app.ui.components.dialog.TextInputDialog
import com.nextstep.app.ui.goals.GoalView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * 목표 카드. 접힌 상태에서는 진행률과 이번 구간의 단계만, 펼치면 구간별 전체 단계와 관리 버튼을 보여 줍니다.
 */
@Composable
fun GoalCard(
    view: GoalView,
    periodLabel: (String) -> String,
    currentPeriodKey: String?,
    expanded: Boolean,
    onToggle: () -> Unit,
    canManage: Boolean,
    onSetStepStatus: (GoalStepEntity, MilestoneStatus) -> Unit,
    onSendToTasks: (GoalStepEntity) -> Unit,
    onAddStep: (periodKey: String, title: String) -> Unit,
    onSetGoalStatus: (GoalStatus) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var addStepFor by remember { mutableStateOf<String?>(null) }
    AppCard(modifier = modifier, onClick = onToggle) {
        Column {
            Text(GoalArea.from(view.goal.area).label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(view.goal.title, style = MaterialTheme.typography.titleMedium)
            if (view.goal.description.isNotBlank() && expanded) Text(view.goal.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            LabeledProgress(label = "단계 ${view.doneCount}/${view.steps.size}", ratio = view.progress, color = MaterialTheme.colorScheme.primary)
            if (!expanded) {
                view.currentSteps.take(2).forEach { step ->
                    GoalStepRow(step = step, goalTitle = null, onSetStatus = { onSetStepStatus(step, it) }, onSendToTasks = if (canManage) ({ onSendToTasks(step) }) else null)
                }
                if (view.currentSteps.isEmpty() && !view.isComplete) Text("이번 구간에 할 단계가 없어요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                view.steps.groupBy { it.periodKey }.forEach { (periodKey, steps) ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        periodLabel(periodKey), style = MaterialTheme.typography.labelMedium,
                        color = if (periodKey == currentPeriodKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    steps.forEach { step ->
                        GoalStepRow(step = step, goalTitle = null, onSetStatus = { onSetStepStatus(step, it) }, onSendToTasks = if (canManage) ({ onSendToTasks(step) }) else null)
                    }
                }
                if (canManage) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        currentPeriodKey?.let { TextButton(onClick = { addStepFor = it }) { Text("이번 구간에 단계 추가") } }
                        if (view.goal.status == GoalStatus.ACTIVE) TextButton(onClick = { onSetGoalStatus(GoalStatus.ARCHIVED) }) { Text("보관") }
                        else TextButton(onClick = { onSetGoalStatus(GoalStatus.ACTIVE) }) { Text("다시 진행") }
                        TextButton(onClick = onDelete) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
    addStepFor?.let { periodKey ->
        TextInputDialog(title = "${periodLabel(periodKey)} 단계", label = "예: 리더스 2단계 10권", onConfirm = { onAddStep(periodKey, it); addStepFor = null }, onDismiss = { addStepFor = null })
    }
}
