package com.nextstep.app.ui.components.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.project.RoutineItem
import com.nextstep.app.ui.common.ratio
import com.nextstep.app.ui.components.icon.routineIcon

/**
 * 오늘의 루틴: 진행 중인 교육 프로젝트마다 지금 단계의 루틴 줄과 이번 주 채운 양.
 * 줄을 누르면 그 분량만큼 "했어요"로 남고, 다시 누르면 취소됩니다. 제목을 누르면 프로젝트 화면으로 갑니다.
 * [big] 은 어린 학생 화면(숫자 대신 큰 줄)입니다. 목록은 3개까지(UX 가이드 1-5).
 */
@Composable
fun RoutineCard(items: List<ProjectProgress>, onToggle: (ProjectProgress, RoutineItem) -> Unit, onOpen: (String) -> Unit, big: Boolean = false) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items.forEach { p ->
                p.current?.let { phase ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.clickable { onOpen(p.goalId) }, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.plan.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${p.currentIndex + 1}단계 · ${phase.title}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (!big) PaceChip(p.pace)
                        }
                        phase.routine.forEach { item ->
                            RoutineCheckRow(item, done = item.name in p.todayDoneItems, big = big, onClick = { onToggle(p, item) })
                        }
                        if (!big) {
                            LabeledProgress(
                                label = "이번 주 ${p.weekMinutes}/${p.weekTarget}분", ratio = ratio(p.weekMinutes, p.weekTarget).coerceAtMost(1f),
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineCheckRow(item: RoutineItem, done: Boolean, big: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.clickable(onClick = onClick).heightIn(min = if (big) 56.dp else 40.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, contentDescription = if (done) "했어요" else "아직",
            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(if (big) 32.dp else 22.dp),
        )
        Icon(routineIcon(item.kind), contentDescription = item.kind.label, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.name, style = if (big) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                textDecoration = if (done) TextDecoration.LineThrough else null, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(item.amountLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
