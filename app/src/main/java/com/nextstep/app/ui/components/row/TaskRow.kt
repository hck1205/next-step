package com.nextstep.app.ui.components.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag

@Composable
fun TaskRow(task: TaskEntity, subjects: List<SubjectEntity>, onToggle: () -> Unit, onDelete: (() -> Unit)? = null) {
    val subject = subjects.firstOrNull { it.id == task.subjectId }
    val overdue = !task.done && task.dueDate < DateUtils.today().toEpochDay()
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f)) {
                Text(
                    task.title, style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                    color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(task.type.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (subject != null) SubjectTag(subject)
                    AssignedByLabel(task)
                    Text(
                        (if (overdue) "기한 지남 · " else "") + DateUtils.formatDate(DateUtils.fromEpochDay(task.dueDate)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (onDelete != null) TextButton(onClick = onDelete) { Text("삭제") }
        }
    }
}
