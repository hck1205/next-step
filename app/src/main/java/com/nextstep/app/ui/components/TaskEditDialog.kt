package com.nextstep.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

@Composable
fun TaskEditDialog(existing: TaskEntity?, subjects: List<SubjectEntity>, defaultDate: LocalDate, onDismiss: () -> Unit, onSave: (String, String?, TaskType, LocalDate) -> Unit) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var subjectId by remember { mutableStateOf(existing?.subjectId) }
    var type by remember { mutableStateOf(existing?.type ?: TaskType.HOMEWORK) }
    var due by remember { mutableStateOf(existing?.let { DateUtils.fromEpochDay(it.dueDate) } ?: defaultDate) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "할 일 추가" else "할 일 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("할 일") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OptionPicker(TaskType.entries, type, label = { it.label }, onSelect = { type = it })
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                DateField("마감", due, onChange = { due = it })
            }
        },
        confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onSave(title.trim(), subjectId, type, due); onDismiss() }) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
