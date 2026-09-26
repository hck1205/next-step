package com.nextstep.app.ui.projects.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.ratio
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress
import com.nextstep.app.ui.components.card.PaceChip

/** 진행 중인 프로젝트 한 장: 분류 · 목표 문장 · 단계 진행 · 지금 단계의 하루 양 · 예상 도착일. */
@Composable
internal fun ProjectCard(p: ProjectProgress, onOpen: () -> Unit) {
    AppCard(onClick = onOpen) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${p.plan.category.label} · ${p.plan.span}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                PaceChip(p.pace)
            }
            Text(p.plan.title, style = MaterialTheme.typography.titleMedium)
            Text(p.plan.goal, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            LabeledProgress(
                label = if (p.isDone) "모든 단계 통과" else "${p.currentIndex + 1}/${p.total}단계 · ${p.current?.title}",
                ratio = ratio(p.currentIndex, p.total), color = MaterialTheme.colorScheme.primary,
            )
            p.current?.let { phase ->
                Text(
                    "지금: 하루 약 ${phase.dailyMinutes}분 · 이번 주 ${p.weekMinutes}/${p.weekTarget}분" +
                        (p.projectedEnd?.let { " · 도착 예상 ${DateUtils.formatMonth(it)}" } ?: ""),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
