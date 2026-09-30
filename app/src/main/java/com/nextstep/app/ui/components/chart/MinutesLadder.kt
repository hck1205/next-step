package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.project.ProjectPhase

/**
 * 단계별 하루 평균 분을 계단처럼 보여 주는 막대. "조금씩 늘려 간다"를 한눈에 보이게 합니다.
 * [current] 단계는 진한 색, 지난 단계는 옅은 색, [skippedBefore] 앞 단계는 회색입니다.
 */
@Composable
fun MinutesLadder(phases: List<ProjectPhase>, current: Int?, modifier: Modifier = Modifier, skippedBefore: Int = 0) {
    val max = phases.maxOfOrNull { it.dailyMinutes }?.coerceAtLeast(1) ?: 1
    val scheme = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
        phases.forEachIndexed { i, phase ->
            val color = when {
                i < skippedBefore -> scheme.surfaceVariant
                current != null && i == current -> scheme.primary
                current != null && i < current -> scheme.primary.copy(alpha = 0.35f)
                else -> scheme.secondaryContainer
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${phase.dailyMinutes}", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Box(
                    Modifier.fillMaxWidth().height((BAR_MIN + (BAR_MAX - BAR_MIN) * phase.dailyMinutes / max).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(color),
                )
                Text(phase.ageLabel, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1, textAlign = TextAlign.Center)
            }
        }
    }
}

private const val BAR_MIN = 6
private const val BAR_MAX = 72
