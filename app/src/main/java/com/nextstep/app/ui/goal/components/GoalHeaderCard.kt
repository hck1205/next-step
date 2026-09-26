package com.nextstep.app.ui.goal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.domain.goaltree.GoalNode
import com.nextstep.app.domain.goaltree.Assigner
import com.nextstep.app.ui.common.asPercent
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.DDayBadge
import com.nextstep.app.ui.components.card.LabeledProgress

/** 목표 맨 위: 분류 · 누가 만든 목표, 제목 · 이유, 달성률, 기한, 멈춘 날수, 달성/다시 열기/보관/고치기. */
@Composable
internal fun GoalHeaderCard(node: GoalNode, canClose: Boolean, onAchieve: () -> Unit, onReopen: () -> Unit, onArchive: () -> Unit, onEdit: () -> Unit) {
    val g = node.goal
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    listOfNotNull(GoalArea.from(g.area).label, Assigner.of(g.createdByRole)?.goalLabel).joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f),
                )
                node.daysLeft?.takeIf { !node.isAchieved }?.let { DDayBadge(it) }
            }
            Text(g.title, style = MaterialTheme.typography.titleLarge)
            if (g.description.isNotBlank()) Text("왜: ${g.description}", style = MaterialTheme.typography.bodyMedium)
            LabeledProgress(
                label = "달성 ${node.rate.asPercent()} · 할 일 ${node.doneTasks}/${node.totalTasks}" +
                    (if (node.children.isNotEmpty()) " · 작은 목표 ${node.achievedChildren}/${node.children.size}" else ""),
                ratio = node.rate, color = MaterialTheme.colorScheme.primary,
            )
            Text(
                listOfNotNull(
                    g.targetDate?.let { "기한 ${DateUtils.formatDate(DateUtils.fromEpochDay(it))}" },
                    when (g.status) {
                        GoalStatus.DONE -> g.doneAt?.let { "${DateUtils.formatDate(DateUtils.toLocalDate(it))} 달성" } ?: "달성"
                        GoalStatus.ARCHIVED -> "보관함"
                        GoalStatus.ACTIVE -> if (node.isIdle) "${node.idleDays}일째 그대로" else "마지막 진행 ${DateUtils.formatDate(node.lastActivity)}"
                    },
                    if (node.overdue > 0) "밀린 할 일 ${node.overdue}개" else null,
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (canClose) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    when (g.status) {
                        GoalStatus.ACTIVE -> if (node.readyToAchieve) Button(onClick = onAchieve) { Text("달성했어요") } else OutlinedButton(onClick = onAchieve) { Text("달성으로 표시") }
                        else -> OutlinedButton(onClick = onReopen) { Text("다시 열기") }
                    }
                    TextButton(onClick = onEdit) { Text("고치기") }
                    if (g.status == GoalStatus.ACTIVE) TextButton(onClick = onArchive) { Text("보관") }
                }
            }
        }
    }
}

