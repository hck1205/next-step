package com.nextstep.app.ui.goaltree.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.goaltree.GoalNode
import com.nextstep.app.domain.goaltree.PlanHistory
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.DDayBadge
import com.nextstep.app.ui.components.card.LabeledProgress

/**
 * 목표 한 장: 분류 · 만든 사람, 제목, 달성률(할 일 + 작은 목표), 기한, 밀린 할 일 · 멈춘 날수, 모두 끝냈으면 "달성 표시만 남았어요".
 * [depth] 만큼 들여 써서 큰 목표 아래 작은 목표가 이어지는 모양을 보여 줍니다.
 */
@Composable
internal fun GoalNodeCard(node: GoalNode, depth: Int, onOpen: () -> Unit) {
    val g = node.goal
    AppCard(modifier = Modifier.padding(start = (depth * INDENT).dp), onClick = onOpen) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (if (depth > 0) "↳ " else "") + "${GoalArea.entries.firstOrNull { it.name == g.area }?.label ?: "목표"} · ${Role.from(g.createdByRole)?.let { PlanHistory.assignerLabel(it) } ?: ""} 만듦",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f),
                )
                node.daysLeft?.takeIf { !node.isAchieved }?.let { DDayBadge(it) }
            }
            Text(g.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            LabeledProgress(
                label = "달성 ${(node.rate * PERCENT).toInt()}% · 할 일 ${node.doneTasks}/${node.totalTasks}" +
                    (if (node.children.isNotEmpty()) " · 작은 목표 ${node.achievedChildren}/${node.children.size}" else ""),
                ratio = node.rate, color = MaterialTheme.colorScheme.primary,
            )
            val warn = when {
                node.readyToAchieve -> "모두 끝냈어요 · 달성 표시만 남았어요"
                node.overdue > 0 -> "밀린 할 일 ${node.overdue}개"
                !node.isAchieved && node.idleDays >= IDLE_DAYS -> "${node.idleDays}일째 그대로예요"
                node.totalTasks == 0 && node.children.isEmpty() && !node.isAchieved -> "세부 할 일을 넣어 시작해요"
                else -> null
            }
            warn?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = if (node.readyToAchieve) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

private const val INDENT = 16
private const val PERCENT = 100
private const val IDLE_DAYS = 7
