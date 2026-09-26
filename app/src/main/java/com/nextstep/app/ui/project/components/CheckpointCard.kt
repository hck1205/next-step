package com.nextstep.app.ui.project.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.project.ProjectPhase
import com.nextstep.app.ui.components.card.AppCard

/** 지금 단계의 통과 기준 · 추천 교재 · 한마디. 통과하면 다음 단계의 루틴으로 바뀝니다. */
@Composable
internal fun CheckpointCard(phase: ProjectPhase, isLast: Boolean, onPass: (() -> Unit)?) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("이 단계를 넘는 기준", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(phase.checkpoint, style = MaterialTheme.typography.titleSmall)
            Text("준비물 · ${phase.materials}", style = MaterialTheme.typography.bodySmall)
            Text(phase.tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("보통 ${phase.weeks}주 · 하루 약 ${phase.dailyMinutes}분", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            onPass?.let { Button(onClick = it) { Text(if (isLast) "통과했어요 · 목표 달성" else "통과했어요 · 다음 단계로") } }
        }
    }
}
