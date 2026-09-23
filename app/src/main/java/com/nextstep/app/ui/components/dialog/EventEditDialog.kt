package com.nextstep.app.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.TimeField
import com.nextstep.app.ui.components.input.OptionPicker
import com.nextstep.app.ui.components.input.SubjectPicker

@Composable
fun EventEditDialog(
    existing: EventEntity?,
    subjects: List<SubjectEntity>,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (String, String?, EventType, LocalDate, LocalTime, LocalTime, Boolean, String, String) -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var subjectId by remember { mutableStateOf(existing?.subjectId) }
    var type by remember { mutableStateOf(existing?.type ?: EventType.CLASS) }
    var date by remember { mutableStateOf(existing?.let { DateUtils.toLocalDate(it.startAt) } ?: defaultDate) }
    var start by remember { mutableStateOf(existing?.let { DateUtils.toLocalDateTime(it.startAt).toLocalTime() } ?: LocalTime.of(16, 0)) }
    var end by remember { mutableStateOf(existing?.let { DateUtils.toLocalDateTime(it.endAt).toLocalTime() } ?: LocalTime.of(17, 0)) }
    var repeat by remember { mutableStateOf(existing?.repeatWeekly ?: false) }
    var location by remember { mutableStateOf(existing?.location ?: "") }
    var memo by remember { mutableStateOf(existing?.memo ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "일정 추가" else "일정 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OptionPicker(EventType.entries, type, label = { it.label }, onSelect = { type = it })
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                DateField("날짜", date, onChange = { date = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeField("시작", start, onChange = { start = it }, modifier = Modifier.weight(1f))
                    TimeField("종료", end, onChange = { end = it }, modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("매주 반복 (시간표)", modifier = Modifier.weight(1f))
                    Switch(checked = repeat, onCheckedChange = { repeat = it })
                }
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("장소 (선택)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모 (선택)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = { onSave(title.trim(), subjectId, type, date, start, end, repeat, location.trim(), memo.trim()); onDismiss() }) { Text("저장") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = { onDelete(); onDismiss() }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        },
    )
}
