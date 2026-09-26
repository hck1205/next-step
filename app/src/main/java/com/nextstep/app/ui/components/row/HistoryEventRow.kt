package com.nextstep.app.ui.components.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.goaltree.HistoryEvent
import com.nextstep.app.domain.goaltree.HistoryKind
import com.nextstep.app.domain.goaltree.Assigner
import com.nextstep.app.domain.time.DateUtils

/** 기록 한 줄: 날짜 · 무엇이(목표 시작 · 할 일 끝 · 목표 달성) · 어느 목표의 일인지 · 달성이 어느 목표로 이어졌는지. */
@Composable
fun HistoryEventRow(e: HistoryEvent) {
    val (icon, tint) = when (e.kind) {
        HistoryKind.GOAL_STARTED -> Icons.Default.Flag to MaterialTheme.colorScheme.secondary
        HistoryKind.TASK_DONE -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        HistoryKind.GOAL_ACHIEVED -> Icons.Default.EmojiEvents to MaterialTheme.colorScheme.tertiary
    }
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(DateUtils.formatShortDate(e.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(icon, contentDescription = e.kind.label, tint = tint, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f)) {
            Text(e.title, style = MaterialTheme.typography.bodyMedium)
            val who = Assigner.of(e.byRole)
            val sub = listOfNotNull(
                e.kind.label, who?.let { if (e.kind == HistoryKind.TASK_DONE) it.taskLabel else it.goalLabel },
                e.goalTitle?.let { "목표: $it" }, e.leadsToTitle?.let { "→ $it" },
            )
            Text(sub.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
