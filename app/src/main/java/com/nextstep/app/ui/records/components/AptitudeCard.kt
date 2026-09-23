package com.nextstep.app.ui.records.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.domain.insight.AptitudeSignal
import com.nextstep.app.ui.components.AppCard

/** 예체능·비교과 소질 신호 카드: 영역별 근거와 다음 한 걸음, 최근 관찰 메모. */
@Composable
fun AptitudeCard(signals: List<AptitudeSignal>, observations: List<ObservationEntity>, onObserve: (() -> Unit)?, onDeleteObservation: ((String) -> Unit)?, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("예체능·비교과 소질 신호", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (signals.isEmpty()) "활동 기록과 관찰 메모가 쌓이면 어느 쪽에 소질이 보이는지 알려 드려요"
                        else signals.joinToString(" · ") { "${it.domain.label} ${STAGE_LABELS.getValue(it.stage)}" },
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                if (onObserve != null) TextButton(onClick = onObserve) { Text("관찰") }
            }
            signals.forEach { s ->
                Spacer(Modifier.height(2.dp))
                Text("${s.domain.label} · ${STAGE_LABELS.getValue(s.stage)}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("근거: ${s.evidence.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                Text("다음: ${s.nextStep}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (observations.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("최근 관찰", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                observations.forEach { o ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${o.domain.label} · ${o.text}" + (o.authorName.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        if (onDeleteObservation != null) TextButton(onClick = { onDeleteObservation(o.id) }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
            Text("신호는 점수가 아니라 '더 해 볼 이유'예요. 키·몸무게 같은 신체 수치는 판단에 쓰지 않아요.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private val STAGE_LABELS = mapOf(1 to "체험 더 해 보기", 2 to "정기 활동으로", 3 to "무대·대회 도전")
