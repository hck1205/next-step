package com.nextstep.app.ui.projectcatalog.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.project.ProjectPlan
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.MinutesLadder

/**
 * 고를 수 있는 프로젝트 한 장: 분류 · 기간 · 목표 문장 · 하루 양의 범위와 총 시간.
 * "자세히"를 누르면 단계별 하루 양 막대와 단계 목록(통과 기준)이 펼쳐집니다.
 */
@Composable
internal fun PlanCard(plan: ProjectPlan, suggestedIndex: Int, started: Boolean, onStart: (() -> Unit)?) {
    var expanded by remember { mutableStateOf(false) }
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${plan.category.label} · ${plan.span}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(plan.title, style = MaterialTheme.typography.titleMedium)
            Text(plan.goal, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${plan.phases.size}단계 · 하루 ${plan.minDailyMinutes}분 → ${plan.maxDailyMinutes}분 · 모두 약 ${plan.totalHours}시간",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            plan.phases.getOrNull(suggestedIndex)?.let { phase ->
                Text("지금 나이면 ${suggestedIndex + 1}단계 \"${phase.title}\"부터", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
            if (expanded) {
                MinutesLadder(plan.phases, current = suggestedIndex)
                Text(plan.why, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                plan.phases.forEachIndexed { i, phase ->
                    Column {
                        Text("${i + 1}. ${phase.title} · ${phase.ageLabel} · 하루 약 ${phase.dailyMinutes}분", style = MaterialTheme.typography.labelLarge)
                        Text(phase.routine.joinToString(" · ") { "${it.name} ${it.amountLabel}" }, style = MaterialTheme.typography.bodySmall)
                        Text("통과: ${phase.checkpoint}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "접기" else "단계 모두 보기") }
                Spacer(Modifier.weight(1f))
                when {
                    started -> Text("진행 중", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    onStart != null -> TextButton(onClick = onStart) { Text("시작하기") }
                }
            }
        }
    }
}
