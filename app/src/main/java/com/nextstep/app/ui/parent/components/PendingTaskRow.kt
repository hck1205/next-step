package com.nextstep.app.ui.parent.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag
import com.nextstep.app.ui.components.row.AssignedByLabel

/** 학부모가 보는 자녀 할 일 한 줄. 체크는 학생만 하므로 읽기 전용. */
@Composable
internal fun PendingTaskRow(task: TaskEntity, subject: SubjectEntity?) {
    AppCard {
        Column {
            Text(task.title, style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(task.type.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                if (subject != null) SubjectTag(subject)
                Text(DateUtils.formatDate(DateUtils.fromEpochDay(task.dueDate)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AssignedByLabel(task)
            }
        }
    }
}
