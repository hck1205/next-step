package com.nextstep.app.ui.project.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.project.PhaseSlot
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard

/** 단계 일정: 통과(✓) · 지금(▶) · 앞으로(○) · 건너뜀(−). 지금과 앞으로의 단계는 루틴 요약까지 보입니다. */
@Composable
internal fun PhaseTimeline(slots: List<PhaseSlot>, currentIndex: Int, skipped: Int) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            slots.forEach { slot ->
                val i = slot.index
                val scheme = MaterialTheme.colorScheme
                val (icon, tint) = when {
                    i < skipped -> Icons.Default.RemoveCircleOutline to scheme.outline
                    i < currentIndex -> Icons.Default.CheckCircle to scheme.primary
                    i == currentIndex -> Icons.Default.PlayCircle to scheme.primary
                    else -> Icons.Default.RadioButtonUnchecked to scheme.outline
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${i + 1}. ${slot.phase.title} · ${slot.phase.ageLabel}", style = MaterialTheme.typography.labelLarge)
                        val start = slot.start
                        val end = slot.end
                        val dates = if (start != null && end != null) "${DateUtils.formatMonth(start)} ~ ${DateUtils.formatMonth(end)}" else "이미 할 수 있어 건너뜀"
                        Text(dates, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                        if (i >= currentIndex && i >= skipped) {
                            Text(slot.phase.routine.joinToString(" · ") { "${it.name} ${it.amountLabel}" }, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
