package com.nextstep.app.ui.assignments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import java.time.LocalDate

/** 과제 한 줄: 과목 · 제목 · 마감(D-day). 여기서는 보기만 하고, 끝내기는 학생 화면에서. */
@Composable
internal fun AssignmentRow(task: TaskEntity, subjects: List<SubjectEntity>, today: LocalDate) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                subjects.firstOrNull { it.id == task.subjectId }?.let { SubjectTag(it) }
                Text(task.title, style = MaterialTheme.typography.bodyLarge, textDecoration = if (task.done) TextDecoration.LineThrough else null)
            }
            val due = DateUtils.fromEpochDay(task.dueDate)
            Text(
                if (task.done) "끝냄" else DateUtils.dDay(due, today),
                style = MaterialTheme.typography.labelMedium,
                color = if (!task.done && due.isBefore(today)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
