package com.nextstep.app.ui.journey.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.JourneyPhase
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.AppCard
import java.time.LocalDate

/**
 * 이정표 한 줄. 체크로 완료, 펼치면 이유·메모·건너뛰기·날짜 변경. 정책 없이 콜백만 올립니다.
 */
@Composable
fun MilestoneRow(
    item: JourneyItem,
    today: LocalDate,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onSetStatus: (MilestoneStatus) -> Unit,
    onEditNote: () -> Unit,
    onEditDate: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val phase = item.phase(today)
    val done = item.status == MilestoneStatus.DONE
    val muted = done || item.status == MilestoneStatus.SKIPPED
    AppCard(modifier = modifier, onClick = onToggleExpand) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = done, onCheckedChange = { onSetStatus(if (it) MilestoneStatus.DONE else MilestoneStatus.UPCOMING) })
                Column(Modifier.weight(1f)) {
                    Text(
                        item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (item.priority == 1 && !muted) FontWeight.SemiBold else FontWeight.Normal,
                        textDecoration = if (muted) TextDecoration.LineThrough else null,
                        color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "${item.category.label} · ${dueLabel(item, phase, today)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (phase == JourneyPhase.OVERDUE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.priority == 1 && !muted) AssistChip(onClick = onToggleExpand, label = { Text("놓치면 안 돼요") })
            }
            if (expanded) {
                Spacer(Modifier.height(4.dp))
                Text(item.description, style = MaterialTheme.typography.bodyMedium)
                if (item.why.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("왜 지금? ${item.why}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                if (item.note.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("메모: ${item.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onEditNote) { Text("메모") }
                    TextButton(onClick = onEditDate) { Text("날짜") }
                    if (item.status == MilestoneStatus.SKIPPED) TextButton(onClick = { onSetStatus(MilestoneStatus.UPCOMING) }) { Text("다시 보기") }
                    else if (!done) TextButton(onClick = { onSetStatus(MilestoneStatus.SKIPPED) }) { Text("건너뛰기") }
                    Spacer(Modifier.width(4.dp))
                    if (onDelete != null) TextButton(onClick = onDelete) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

private fun dueLabel(item: JourneyItem, phase: JourneyPhase, today: LocalDate): String {
    val due = DateUtils.formatDate(item.dueDate)
    return when (phase) {
        JourneyPhase.OVERDUE -> "$due 지남"
        JourneyPhase.NOW -> "$due 까지 · 지금 준비"
        JourneyPhase.UPCOMING -> "$due · ${DateUtils.formatDate(item.startDate)}부터 준비"
        JourneyPhase.DONE -> "완료"
        JourneyPhase.SKIPPED -> "건너뜀"
    }.let { if (today == item.dueDate) "오늘 마감" else it }
}
