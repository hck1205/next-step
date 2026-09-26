package com.nextstep.app.ui.project.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.project.ProjectPace
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.ratio
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress
import com.nextstep.app.ui.components.card.PaceChip

/** 프로젝트 맨 위: 목표 문장, 단계 진행, 계획한 도착일과 지금 속도로 본 도착 예상, 이번 주 채운 양. */
@Composable
internal fun ProjectHeaderCard(p: ProjectProgress) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${p.plan.category.label} · ${p.plan.span}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                PaceChip(p.pace)
            }
            Text("목표", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(p.plan.goal, style = MaterialTheme.typography.titleMedium)
            LabeledProgress(
                label = if (p.isDone) "${p.total}단계 모두 통과" else "${p.currentIndex + 1}/${p.total}단계 · ${p.current?.title}",
                ratio = ratio(p.currentIndex, p.total), color = MaterialTheme.colorScheme.primary,
            )
            p.targetDate?.let { target ->
                Text(
                    "계획한 도착 ${DateUtils.formatMonth(target)}" +
                        (p.projectedEnd?.takeIf { p.pace != ProjectPace.ON_TRACK }?.let { " · 지금 속도면 ${DateUtils.formatMonth(it)}" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (!p.isDone) {
                LabeledProgress(
                    label = "이번 주 ${p.weekMinutes}/${p.weekTarget}분 · ${p.activeDaysThisWeek}일", ratio = ratio(p.weekMinutes, p.weekTarget).coerceAtMost(1f),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}
