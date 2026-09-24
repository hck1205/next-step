package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import com.nextstep.app.domain.growth.StudentWords
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.home.HomeUiState

/**
 * 학생 첫 화면 맨 위의 타이머. [big] 이면(어린 단계) 화면 폭 전체의 큰 시작 버튼 하나와 짧은 말뿐입니다.
 */
@Composable
internal fun TimerCard(state: HomeUiState, words: StudentWords, big: Boolean, onOpenTimer: () -> Unit) {
    val running = state.runningTimer
    val subject = running?.let { r -> state.subjects.firstOrNull { it.id == r.subjectId } }
    AppCard(onClick = onOpenTimer) {
        if (big) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (running != null) "공부하는 중 · ${subject?.name ?: "자유 공부"}" else words.timerIdle, style = MaterialTheme.typography.titleMedium)
                Button(onClick = onOpenTimer, modifier = Modifier.fillMaxWidth().height(BIG_BUTTON_DP.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (running != null) "보기" else words.timerStart, style = MaterialTheme.typography.titleLarge)
                }
            }
        } else Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (running != null) {
                    Text("공부 중 · ${subject?.name ?: "과목 없음"}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("${DateUtils.formatTime(running.startedAt)}부터 기록 중", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("학습 타이머", style = MaterialTheme.typography.titleMedium)
                    Text(words.timerIdle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(onClick = onOpenTimer) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(if (running != null) "열기" else words.timerStart)
            }
        }
    }
}

/** UX 가이드: 학생 타이머 버튼은 76px 이상. */
private const val BIG_BUTTON_DP = 76
