package com.nextstep.app.ui.components.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.goaltree.GoalNode

/**
 * 오늘 화면의 목표 진행(어른이 지켜보기): 먼저 챙길 목표 3개까지. 목표마다 달성률과, 밀린 할 일 · 멈춘 날수 · 달성 표시만 남음 중 하나.
 * 눌러서 목표 화면으로 갑니다.
 */
@Composable
fun GoalFocusCard(goals: List<GoalNode>, onOpen: (String) -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            goals.forEach { n ->
                Column(Modifier.clickable { onOpen(n.goal.id) }) {
                    Text(n.goal.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    LabeledProgress(label = "${(n.rate * PERCENT).toInt()}% · 할 일 ${n.doneTasks}/${n.totalTasks}", ratio = n.rate, color = MaterialTheme.colorScheme.primary)
                    val note = when {
                        n.readyToAchieve -> "모두 끝냈어요 · 달성 표시만 남았어요"
                        n.overdue > 0 -> "밀린 할 일 ${n.overdue}개"
                        n.idleDays >= IDLE_DAYS -> "${n.idleDays}일째 그대로예요"
                        else -> n.pending.firstOrNull()?.let { "다음: ${it.title}" }
                    }
                    note?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = if (n.overdue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private const val PERCENT = 100
private const val IDLE_DAYS = 7
