package com.nextstep.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity

/** 담당 과목 다중 선택. 아무것도 고르지 않으면 전 과목 담당. */
@Composable
fun SubjectSelectDialog(subjects: List<SubjectEntity>, initial: List<String>, onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    val selected = remember { mutableStateOf(initial.toSet()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("담당 과목 선택") },
        text = {
            Column {
                Text("아무것도 선택하지 않으면 전 과목을 담당합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                subjects.forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = s.id in selected.value,
                            onCheckedChange = { checked -> selected.value = if (checked) selected.value + s.id else selected.value - s.id },
                        )
                        ColorDot(subjectColor(s.color), 10)
                        Spacer(Modifier.width(8.dp))
                        Text(s.name)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(subjects.map { it.id }.filter { it in selected.value }); onDismiss() }) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
