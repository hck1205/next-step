package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.home.HomeUiState

@Composable
internal fun TimerCard(state: HomeUiState, onOpenTimer: () -> Unit) {
    val running = state.runningTimer
    AppCard(onClick = onOpenTimer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (running != null) {
                    val subject = state.subjects.firstOrNull { it.id == running.subjectId }
                    Text("공부 중 · ${subject?.name ?: "과목 없음"}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("${DateUtils.formatTime(running.startedAt)}부터 기록 중", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("학습 타이머", style = MaterialTheme.typography.titleMedium)
                    Text("공부를 시작할 때 눌러서 시간을 기록하세요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(onClick = onOpenTimer) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(if (running != null) "열기" else "시작")
            }
        }
    }
}
