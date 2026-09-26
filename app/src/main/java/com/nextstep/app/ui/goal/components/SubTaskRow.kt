package com.nextstep.app.ui.goal.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.goaltree.PlanHistory
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/** 세부 할 일 한 줄: 체크 · 제목 · 누가 준 일 · 마감(밀렸으면 빨강) 또는 끝낸 날 · 과목. */
@Composable
internal fun SubTaskRow(task: TaskEntity, subjects: List<SubjectEntity>, today: LocalDate, canCheck: Boolean, onToggle: () -> Unit, onDelete: (() -> Unit)?) {
    val overdue = !task.done && task.dueDate < today.toEpochDay()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = task.done, onCheckedChange = { onToggle() }, enabled = canCheck)
        Column(Modifier.weight(1f)) {
            Text(task.title, style = MaterialTheme.typography.bodyMedium, textDecoration = if (task.done) TextDecoration.LineThrough else null)
            val who = Role.from(task.createdByRole)?.let { if (it == Role.STUDENT) "스스로 정한 일" else PlanHistory.assignerLabel(it) + " 준 일" }
            val whenLine = if (task.done) task.doneAt?.let { "${DateUtils.formatDate(DateUtils.toLocalDate(it))} 끝냄" } ?: "끝냄"
            else (if (overdue) "밀림 · " else "") + "마감 ${DateUtils.formatDate(DateUtils.fromEpochDay(task.dueDate))}"
            Text(
                listOfNotNull(who, whenLine, subjects.firstOrNull { it.id == task.subjectId }?.name, task.type.label).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall, color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        onDelete?.let { IconButton(onClick = it) { Icon(Icons.Default.Close, contentDescription = "${task.title} 지우기", modifier = Modifier) } }
    }
}
