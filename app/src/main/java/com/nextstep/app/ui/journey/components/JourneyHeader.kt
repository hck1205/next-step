package com.nextstep.app.ui.journey.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.card.LabeledProgress
import com.nextstep.app.ui.journey.JourneyEvent
import com.nextstep.app.ui.journey.JourneyUiState

@Composable
internal fun JourneyHeader(state: JourneyUiState, onEvent: (JourneyEvent) -> Unit) {
    AppCard {
        Column {
            if (!state.hasBirthDate) {
                Text("생년월일로 여정을 시작해요", style = MaterialTheme.typography.titleMedium)
                Text("어린이집 대기, 예방접종, 유치원 지원, 언어 민감기, 학기별 목표, 입시 일정까지 나이에 맞춰 미리 알려 드려요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                DateField(label = "생년월일", date = state.today.minusYears(3), onChange = { onEvent(JourneyEvent.SetBirthDate(it)) })
            } else {
                Text("${state.ageLabel} · ${state.stage?.label ?: ""}", style = MaterialTheme.typography.titleMedium)
                state.stage?.let { Text(it.focus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Spacer(Modifier.height(8.dp))
                LabeledProgress(label = "지금까지의 여정", ratio = state.completion, color = MaterialTheme.colorScheme.primary, trailing = "${(state.completion * 100).toInt()}%")
                if (state.overdueCount > 0 || state.nowCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        buildString {
                            if (state.overdueCount > 0) append("지난 항목 ${state.overdueCount}개")
                            if (state.overdueCount > 0 && state.nowCount > 0) append(" · ")
                            if (state.nowCount > 0) append("지금 준비할 것 ${state.nowCount}개")
                        },
                        style = MaterialTheme.typography.bodySmall, color = if (state.overdueCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
