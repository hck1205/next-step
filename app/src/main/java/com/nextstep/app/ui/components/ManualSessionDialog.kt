package com.nextstep.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ManualSessionDialog(subjects: List<SubjectEntity>, onDismiss: () -> Unit, onSave: (String?, LocalDate, LocalTime, Int, String) -> Unit) {
    var subjectId by remember { mutableStateOf(subjects.firstOrNull()?.id) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var start by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0).minusHours(1)) }
    var minutes by remember { mutableStateOf("60") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("학습 기록 직접 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                DateField("날짜", date, onChange = { date = it })
                TimeField("시작", start, onChange = { start = it }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = minutes, onValueChange = { minutes = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("학습 시간(분)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("메모") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = (minutes.toIntOrNull() ?: 0) > 0, onClick = { onSave(subjectId, date, start, minutes.toInt(), note); onDismiss() }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
