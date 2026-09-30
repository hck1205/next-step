package com.nextstep.app.ui.goals.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nextstep.app.domain.journey.GoalTrack
import com.nextstep.app.ui.components.card.AppCard

/** 시작할 수 있는 트랙 카드: 제목, 구간 범위, 단계 수. */
@Composable
fun TrackCard(track: GoalTrack, periodLabel: (String) -> String, onStart: (() -> Unit)?, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(track.area.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(track.title, style = MaterialTheme.typography.titleSmall)
                Text("${periodLabel(track.firstPeriodKey)} ~ ${periodLabel(track.lastPeriodKey)} · 단계 ${track.steps.size}개", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(track.description, style = MaterialTheme.typography.bodySmall)
            }
            if (onStart != null) TextButton(onClick = onStart) { Text("시작") }
        }
    }
}
