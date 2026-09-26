package com.nextstep.app.ui.goal.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.goaltree.GoalNode
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress

/**
 * "이 목표를 이루면 →" 이어지는 목표 길(바로 위 → 맨 위)과 각 목표의 달성률. 눌러서 그 목표로 갑니다.
 * 이 목표를 달성했다면 바로 위 목표가 얼마나 올라왔는지 먼저 알려 줍니다.
 */
@Composable
internal fun ChainCard(chain: List<GoalNode>, achieved: Boolean, onOpen: (String) -> Unit, onChange: (() -> Unit)?) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (achieved && chain.isNotEmpty()) "달성이 이어진 목표" else "이 목표를 이루면", style = MaterialTheme.typography.titleSmall)
            if (chain.isEmpty()) {
                Text("아직 이어지는 큰 목표가 없어요. 이 목표가 무엇을 위한 것인지 이어 두면 달성이 쌓여 보여요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            chain.forEachIndexed { i, n ->
                Column(Modifier.clickable { onOpen(n.goal.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("→ ".repeat(i + 1) + n.goal.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    LabeledProgress(label = "${(n.rate * PERCENT).toInt()}%", ratio = n.rate, color = MaterialTheme.colorScheme.secondary)
                }
            }
            onChange?.let { TextButton(onClick = it) { Text(if (chain.isEmpty()) "이어지는 목표 정하기" else "이어지는 목표 바꾸기") } }
        }
    }
}

private const val PERCENT = 100
