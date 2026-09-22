package com.nextstep.app.ui.content.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.ui.components.OptionPicker
import com.nextstep.app.ui.content.ContentEvent
import com.nextstep.app.ui.content.ContentUiState

@Composable
internal fun AddContentDialog(state: ContentUiState, onEvent: (ContentEvent) -> Unit, subjectKeys: List<String>, onDismiss: () -> Unit) {
    val add = state.add
    val draft = add.draft
    var title by remember(draft) { mutableStateOf(draft?.title ?: "") }
    var channel by remember(draft) { mutableStateOf(draft?.channel ?: "") }
    var subjectKey by remember(draft) { mutableStateOf(draft?.classification?.subjectKey ?: "") }
    var level by remember(draft) { mutableStateOf(draft?.classification?.gradeLevel ?: GradeLevel.ALL) }
    var type by remember(draft) { mutableStateOf(draft?.classification?.contentType ?: ContentType.OTHER) }
    var keywords by remember(draft) { mutableStateOf(draft?.classification?.keywords?.joinToString(", ") ?: "") }
    var summary by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (draft == null) "유튜브 링크 등록" else "분류 확인") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (draft == null) {
                    OutlinedTextField(value = add.url, onValueChange = { onEvent(ContentEvent.SetUrl(it)) }, label = { Text("유튜브 링크 붙여넣기") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("영상은 저장하지 않고 링크만 등록해요. 제목과 채널을 읽어 과목·학년·유형·키워드를 자동으로 붙입니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    add.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    if (add.loading) CircularProgressIndicator(Modifier.width(24.dp).height(24.dp))
                } else {
                    if (!draft.metadataFetched) Text("제목을 자동으로 가져오지 못했어요. 직접 입력해 주세요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = channel, onValueChange = { channel = it }, label = { Text("채널") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    SubjectKeyPicker(subjectKeys, subjectKey) { subjectKey = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OptionPicker(GradeLevel.entries, level, label = { it.label }, onSelect = { level = it }, modifier = Modifier.weight(1f))
                        OptionPicker(ContentType.entries, type, label = { it.label }, onSelect = { type = it }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = keywords, onValueChange = { keywords = it }, label = { Text("키워드 (쉼표 구분, 단원명 등)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("한 줄 설명 (선택)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = minutes, onValueChange = { minutes = it.filter { c -> c.isDigit() }.take(3) }, label = { Text("길이(분, 선택)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    if (draft.classification.reasons.isNotEmpty()) Text("자동 분류 근거: " + draft.classification.reasons.joinToString(" / "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            if (draft == null) Button(onClick = { onEvent(ContentEvent.Analyze) }, enabled = add.url.isNotBlank() && !add.loading) { Text("분석") }
            else TextButton(enabled = title.isNotBlank(), onClick = { onEvent(ContentEvent.Save(title, channel, subjectKey, level, type, keywords, summary, minutes.toIntOrNull() ?: 0)); onDismiss() }) { Text("등록") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
