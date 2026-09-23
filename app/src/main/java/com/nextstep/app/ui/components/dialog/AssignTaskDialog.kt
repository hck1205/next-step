package com.nextstep.app.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.OptionPicker
import com.nextstep.app.ui.components.input.SubjectPicker
import java.time.LocalDate

/** 학부모·멘토가 학생에게 할 일(과제)을 배정하는 다이얼로그. */
@Composable
fun AssignTaskDialog(
    subjects: List<SubjectEntity>,
    title: String = "할 일 배정",
    label: String = "할 일",
    defaultSubjectId: String? = null,
    defaultDue: LocalDate = DateUtils.today(),
    onDismiss: () -> Unit,
    onSave: (String, String?, TaskType, LocalDate) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var subjectId by remember { mutableStateOf(defaultSubjectId) }
    var type by remember { mutableStateOf(TaskType.HOMEWORK) }
    var due by remember { mutableStateOf(defaultDue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                OptionPicker(TaskType.entries, type, label = { it.label }, onSelect = { type = it })
                DateField("마감", due, onChange = { due = it })
            }
        },
        confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onSave(text.trim(), subjectId, type, due); onDismiss() }) { Text("배정") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
