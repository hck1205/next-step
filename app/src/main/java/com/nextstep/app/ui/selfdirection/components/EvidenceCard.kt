package com.nextstep.app.ui.selfdirection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.selfdirection.SelfDirectionReport
import com.nextstep.app.domain.selfdirection.WeekEvidence
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.common.asPercent

/** 최근 4주 흔적: 주마다 계획(스스로 ✓ · 어른이 ○ · 없음 −), 돌아보기, 목표 끝낸 수, 계획 대비 시간. 비교 대상은 아이 자신뿐입니다. */
@Composable
internal fun EvidenceCard(report: SelfDirectionReport) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("최근 4주", style = MaterialTheme.typography.titleSmall)
            Text(
                "스스로 계획 ${report.childPlannedWeeks}주 · 돌아보기 ${report.reflectedWeeks}주" +
                    (report.selfTaskRatio?.let { " · 스스로 만든 할 일 ${it.asPercent()}" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { report.weeks.forEach { WeekCell(it, Modifier.weight(1f)) } }
        }
    }
}

@Composable
private fun WeekCell(w: WeekEvidence, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("${w.weekStart.monthValue}.${w.weekStart.dayOfMonth}", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        val (icon, tint) = when {
            w.childPlanned -> Icons.Default.CheckCircle to scheme.primary
            w.planned -> Icons.Default.RadioButtonUnchecked to scheme.secondary
            else -> Icons.Default.RemoveCircleOutline to scheme.outline
        }
        Icon(icon, contentDescription = if (w.childPlanned) "스스로 계획" else if (w.planned) "어른과 계획" else "계획 없음", tint = tint, modifier = Modifier.size(22.dp))
        Text(if (w.reflected) "돌아봄" else "−", style = MaterialTheme.typography.labelSmall)
        if (w.goalsTotal > 0) Text("${w.goalsDone}/${w.goalsTotal}", style = MaterialTheme.typography.labelSmall)
        w.keptRatio?.let { Text(it.asPercent(), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant) }
    }
}

