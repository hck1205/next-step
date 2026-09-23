package com.nextstep.app.ui.parent.components

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
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.OptionPicker
import com.nextstep.app.ui.components.input.SubjectPicker
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
internal fun AssignTaskDialog(subjects: List<SubjectEntity>, onDismiss: () -> Unit, onSave: (String, String?, TaskType, LocalDate) -> Unit) {
    var title by remember { mutableStateOf("") }
    var subjectId by remember { mutableStateOf<String?>(null) }
    var type by remember { mutableStateOf(TaskType.HOMEWORK) }
    var due by remember { mutableStateOf(LocalDate.now()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("할 일 배정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("할 일") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                OptionPicker(TaskType.entries, type, label = { it.label }, onSelect = { type = it })
                DateField("마감", due, onChange = { due = it })
            }
        },
        confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onSave(title.trim(), subjectId, type, due); onDismiss() }) { Text("배정") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
