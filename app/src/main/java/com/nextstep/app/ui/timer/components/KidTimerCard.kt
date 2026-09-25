package com.nextstep.app.ui.timer.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.input.SubjectPicker
import com.nextstep.app.ui.timer.TimerEvent
import com.nextstep.app.ui.timer.TimerUiState

/**
 * 아이용 타이머: 숫자 대신 줄어드는 원. 올해 한 번 공부 길이([targetMinutes])만큼 색이 차 있다가 다 없어지면 끝입니다.
 * 버튼은 둘뿐: 시작! / 다 했어요(저장). 그만할래요는 작은 글씨로.
 */
@Composable
internal fun KidTimerCard(state: TimerUiState, targetMinutes: Int, onEvent: (TimerEvent) -> Unit) {
    val total = targetMinutes * SECONDS_PER_MINUTE
    val running = state.running != null
    val left = (total - state.elapsedSeconds).coerceAtLeast(0)
    val fraction = if (!running || total == 0L) 1f else left.toFloat() / total
    val fill = if (running) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    AppCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                when {
                    !running -> "${targetMinutes}분 동안 해 볼까요?"
                    left == 0L -> "다 했어요! 눌러서 스티커 받기"
                    else -> "색이 다 없어지면 끝"
                },
                style = MaterialTheme.typography.titleLarge,
            )
            Box(Modifier.size(DIAL_DP.dp).semantics { contentDescription = "남은 시간 ${(left + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE}분" }, contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(color = track)
                    drawArc(color = fill, startAngle = START_ANGLE, sweepAngle = FULL_CIRCLE * fraction, useCenter = true)
                }
            }
            if (!running) {
                SubjectPicker(state.subjects, state.selectedSubjectId, onSelect = { onEvent(TimerEvent.SelectSubject(it)) })
                Button(onClick = { onEvent(TimerEvent.Start) }, modifier = Modifier.fillMaxWidth().height(BUTTON_DP.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(32.dp))
                    Text("시작!", style = MaterialTheme.typography.titleLarge)
                }
            } else {
                Button(onClick = { onEvent(TimerEvent.Stop) }, modifier = Modifier.fillMaxWidth().height(BUTTON_DP.dp)) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(32.dp))
                    Text("다 했어요", style = MaterialTheme.typography.titleLarge)
                }
                TextButton(onClick = { onEvent(TimerEvent.Cancel) }) { Text("그만할래요") }
            }
        }
    }
}

private const val SECONDS_PER_MINUTE = 60L
private const val DIAL_DP = 220
private const val BUTTON_DP = 76
private const val START_ANGLE = -90f
private const val FULL_CIRCLE = 360f
