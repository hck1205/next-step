package com.nextstep.app.ui.rewards.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.gamify.XpLine
import com.nextstep.app.ui.components.card.AppCard

/** 경험치가 어디서 왔는지: 해낸 일 · 꾸준함마다 몇 번 × 몇 점. 깎이는 줄은 없습니다. */
@Composable
internal fun XpBreakdownCard(lines: List<XpLine>, xp: Int) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("경험치 $xp · 해낸 만큼만 쌓이고 깎이지 않아요", style = MaterialTheme.typography.titleSmall)
            if (lines.isEmpty()) Text("할 일을 끝내거나 루틴을 하면 쌓여요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            lines.sortedByDescending { it.xp }.forEach { l ->
                Row {
                    Text("${l.source.label} ${l.count}번", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text("+${l.xp}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
