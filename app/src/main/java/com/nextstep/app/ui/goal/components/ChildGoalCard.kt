package com.nextstep.app.ui.goal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.goaltree.GoalNode
import com.nextstep.app.ui.common.asPercent
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress

/** 이 목표로 이어지는 작은 목표 하나: 제목 · 달성률 · 할 일 수. 누르면 그 목표로 갑니다. */
@Composable
internal fun ChildGoalCard(child: GoalNode, onOpen: () -> Unit) {
    AppCard(onClick = onOpen) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (child.isAchieved) Icon(Icons.Default.CheckCircle, contentDescription = "이뤘어요", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(child.goal.title, style = MaterialTheme.typography.bodyLarge)
            }
            LabeledProgress(label = "${child.rate.asPercent()} · 할 일 ${child.doneTasks}/${child.totalTasks}", ratio = child.rate, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
