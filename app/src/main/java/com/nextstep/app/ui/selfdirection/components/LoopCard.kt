package com.nextstep.app.ui.selfdirection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.selfdirection.LoopStep
import com.nextstep.app.domain.selfdirection.Owner
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.ui.components.card.AppCard

/** 한 바퀴(계획 · 실행 · 점검 · 돌아보기)를 지금 누가 맡는지, 다음 칸에서 바뀌는 걸음은 화살표로. */
@Composable
internal fun LoopCard(stage: SelfDirectionStage) {
    val next = stage.next
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("한 바퀴를 누가 맡나요", style = MaterialTheme.typography.titleSmall)
            LoopStep.entries.forEach { step ->
                val now = stage.owner(step)
                val later = next?.owner(step)?.takeIf { it != now }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(step.label, style = MaterialTheme.typography.bodyMedium)
                        Text(step.question, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        now.label + (later?.let { " → ${it.label}" } ?: ""), style = MaterialTheme.typography.labelLarge,
                        color = if (now == Owner.CHILD) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
