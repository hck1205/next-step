package com.nextstep.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.time.DateUtils

@Composable
fun SessionRow(s: StudySessionEntity, subjects: List<SubjectEntity>, onDelete: (() -> Unit)? = null) {
    val subject = subjects.firstOrNull { it.id == s.subjectId }
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectTag(subject)
                    Spacer(Modifier.width(8.dp))
                    Text(DateUtils.formatMinutes(s.durationMinutes), style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    "${DateUtils.formatDate(DateUtils.toLocalDate(s.startAt))} ${DateUtils.formatTime(s.startAt)} ~ ${DateUtils.formatTime(s.endAt)}" +
                        (if (s.note.isNotBlank()) " · ${s.note}" else ""),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onDelete != null) TextButton(onClick = onDelete) { Text("삭제") }
        }
    }
}
