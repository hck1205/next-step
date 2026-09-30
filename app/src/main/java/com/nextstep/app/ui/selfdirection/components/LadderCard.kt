package com.nextstep.app.ui.selfdirection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.selfdirection.SelfDirectionReport
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.ui.components.card.AppCard

/**
 * 자기주도 사다리 여섯 칸. 지금 칸은 진하게, 지나온 칸은 옅게. 학부모는 칸을 눌러 직접 고를 수 있고([onChoose]),
 * 학년 기본값과 다르게 골랐으면 "학년에 맞추기"로 되돌립니다.
 */
@Composable
internal fun LadderCard(report: SelfDirectionReport, forChild: Boolean, onChoose: ((SelfDirectionStage?) -> Unit)?) {
    val scheme = MaterialTheme.colorScheme
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (forChild) "나의 자기주도 사다리" else "자기주도 사다리", style = MaterialTheme.typography.labelMedium, color = scheme.primary)
            Text("${report.stage.label} · 스스로 맡은 것 ${report.stage.childSteps}/4", style = MaterialTheme.typography.titleMedium)
            Text(if (forChild) report.stage.childDoes else report.stage.adultDoes, style = MaterialTheme.typography.bodySmall)
            SelfDirectionStage.entries.reversed().forEach { st ->
                val here = st == report.stage
                val passed = st < report.stage
                Row(
                    Modifier.then(if (onChoose != null) Modifier.clickable { onChoose(st) } else Modifier),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier.size(if (here) 16.dp else 12.dp).clip(CircleShape)
                            .background(if (here) scheme.primary else if (passed) scheme.primary.copy(alpha = PASSED_ALPHA) else scheme.surfaceVariant),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(st.label + if (st == report.defaultStage) " · 학년 기본" else "", style = if (here) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium)
                        if (here) st.handOverNext?.let { Text("다음에 넘겨받을 것: $it", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant) }
                    }
                    Text(st.span, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
            }
            if (onChoose != null && report.chosen) TextButton(onClick = { onChoose(null) }) { Text("학년에 맞추기") }
        }
    }
}

private const val PASSED_ALPHA = 0.35f
