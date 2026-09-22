package com.nextstep.app.ui.content.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.model.ContentScope
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.ui.components.OptionPicker

@Composable
internal fun EditContentDialog(c: ContentEntity, subjectKeys: List<String>, canDelete: Boolean, onDismiss: () -> Unit, onDelete: () -> Unit, onSave: (ContentEntity) -> Unit) {
    val editable = c.scope == ContentScope.FAMILY
    var title by remember { mutableStateOf(c.title) }
    var subjectKey by remember { mutableStateOf(c.subjectKey) }
    var level by remember { mutableStateOf(c.gradeLevel) }
    var type by remember { mutableStateOf(c.contentType) }
    var keywords by remember { mutableStateOf(c.keywords) }
    var summary by remember { mutableStateOf(c.summary) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editable) "콘텐츠 정보 수정" else "콘텐츠 정보") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(c.url, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                if (c.createdByName.isNotBlank()) Text("등록: ${c.createdByName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, enabled = editable, modifier = Modifier.fillMaxWidth())
                if (editable) SubjectKeyPicker(subjectKeys, subjectKey) { subjectKey = it } else Text("과목: ${c.subjectKey.ifBlank { "-" }}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionPicker(GradeLevel.entries, level, label = { it.label }, onSelect = { if (editable) level = it }, modifier = Modifier.weight(1f))
                    OptionPicker(ContentType.entries, type, label = { it.label }, onSelect = { if (editable) type = it }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = keywords, onValueChange = { keywords = it }, label = { Text("키워드") }, enabled = editable, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("설명") }, enabled = editable, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            if (editable) TextButton(enabled = title.isNotBlank(), onClick = { onSave(c.copy(title = title.trim(), subjectKey = subjectKey.trim(), gradeLevel = level, contentType = type, keywords = keywords, summary = summary.trim())) }) { Text("저장") }
            else TextButton(onClick = onDismiss) { Text("닫기") }
        },
        dismissButton = {
            Row {
                if (canDelete) TextButton(onClick = onDelete) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                if (editable) TextButton(onClick = onDismiss) { Text("취소") }
            }
        },
    )
}
