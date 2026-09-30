package com.nextstep.app.ui.activities.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.icon.StarRow
import java.time.LocalDate

/** 활동 기록 한 줄: 종류·제목·기간·장소·소감. 정책 없이 콜백만 올립니다. */
@Composable
fun ActivityRow(activity: ActivityEntity, onEdit: (() -> Unit)?, onDelete: (() -> Unit)?, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${activity.type.label}${if (activity.isOngoing) " · 진행 중" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(activity.title, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        listOfNotNull(dateRange(activity), activity.place.takeIf { it.isNotBlank() }).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (activity.rating > 0) StarRow(activity.rating, tint = MaterialTheme.colorScheme.tertiary)
                }
                if (activity.note.isNotBlank()) Text(activity.note, style = MaterialTheme.typography.bodySmall)
            }
            if (onDelete != null) {
                androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp))
                TextButton(onClick = onDelete) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

private fun dateRange(activity: ActivityEntity): String {
    val start = DateUtils.formatDate(LocalDate.ofEpochDay(activity.date))
    val end = activity.endDate?.let { DateUtils.formatDate(DateUtils.fromEpochDay(it)) }
    return if (end == null || end == start) start else "$start ~ $end"
}
