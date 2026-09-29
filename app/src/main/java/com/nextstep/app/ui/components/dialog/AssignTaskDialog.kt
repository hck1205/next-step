package com.nextstep.app.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.prefs.LinkedChild
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.OptionPicker
import com.nextstep.app.ui.components.input.SubjectPicker
import java.time.LocalDate

/**
 * 학부모·멘토가 학생에게 할 일(과제)을 배정하는 다이얼로그.
 * [others] 가 있으면(멘토가 맡은 다른 학생) "다른 학생에게도" 고르기가 보이고, 저장은 [onSaveTo] 로 고른 가족 id 와 함께 갑니다.
 */
@Composable
fun AssignTaskDialog(
    subjects: List<SubjectEntity>,
    title: String = "할 일 배정",
    label: String = "할 일",
    defaultSubjectId: String? = null,
    defaultDue: LocalDate = DateUtils.today(),
    others: List<LinkedChild> = emptyList(),
    onSaveTo: ((String, String?, TaskType, LocalDate, List<String>) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (String, String?, TaskType, LocalDate) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var subjectId by remember { mutableStateOf(defaultSubjectId) }
    var type by remember { mutableStateOf(TaskType.HOMEWORK) }
    var due by remember { mutableStateOf(defaultDue) }
    var picked by remember { mutableStateOf(emptySet<String>()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                OptionPicker(TaskType.entries, type, label = { it.label }, onSelect = { type = it })
                DateField("마감", due, onChange = { due = it })
                if (others.isNotEmpty() && onSaveTo != null) OthersPicker(others, picked) { id -> picked = if (id in picked) picked - id else picked + id }
            }
        },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = {
                if (onSaveTo != null && picked.isNotEmpty()) onSaveTo(text.trim(), subjectId, type, due, picked.toList()) else onSave(text.trim(), subjectId, type, due)
                onDismiss()
            }) { Text("배정") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

/** 같은 과제를 함께 받을 다른 학생 고르기(과목은 이름으로 맞추고, 그 학생을 열 때 올라갑니다). */
@Composable
private fun OthersPicker(others: List<LinkedChild>, picked: Set<String>, onToggle: (String) -> Unit) {
    Text("다른 학생에게도", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    others.forEach { child ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = child.familyId in picked, onCheckedChange = { onToggle(child.familyId) })
            Text(child.studentName.ifBlank { "학생" })
        }
    }
}
