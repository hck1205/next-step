package com.nextstep.app.ui.activities.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.ui.components.DateField
import com.nextstep.app.ui.components.OptionPicker
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/** 활동 기록 추가·수정. 종류·제목·날짜(기간)·장소·별점·소감을 받습니다. */
@Composable
fun ActivityEditDialog(existing: ActivityEntity?, today: LocalDate, onConfirm: (ActivityEntity) -> Unit, onDismiss: () -> Unit) {
    var type by remember { mutableStateOf(existing?.type ?: ActivityType.FIELD_TRIP) }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var date by remember { mutableStateOf(existing?.date?.let { LocalDate.ofEpochDay(it) } ?: today) }
    var hasEnd by remember { mutableStateOf(existing?.endDate != null) }
    var endDate by remember { mutableStateOf(existing?.endDate?.let { LocalDate.ofEpochDay(it) } ?: today) }
    var place by remember { mutableStateOf(existing?.place ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var rating by remember { mutableStateOf(existing?.rating ?: 0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "활동 기록" else "활동 수정") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OptionPicker(options = ActivityType.entries, selected = type, label = { it.label }, onSelect = { type = it })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목 (예: 국립과학관 견학, 피아노 학원)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                DateField(label = if (hasEnd) "시작일" else "날짜", date = date, onChange = { date = it })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("기간이 있어요 (여러 날·이어지는 활동)", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Switch(checked = hasEnd, onCheckedChange = { hasEnd = it })
                }
                if (hasEnd) DateField(label = "종료일", date = endDate, onChange = { endDate = it })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = place, onValueChange = { place = it }, label = { Text("장소·기관") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OptionPicker(options = listOf(0, 1, 2, 3, 4, 5), selected = rating, label = { if (it == 0) "별점 없음" else "★".repeat(it) }, onSelect = { rating = it })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("소감·배운 점") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val base = existing ?: ActivityEntity(familyId = "", title = title, date = date.toEpochDay())
                    onConfirm(base.copy(type = type, title = title, date = date.toEpochDay(), endDate = if (hasEnd) endDate.toEpochDay() else null, place = place, note = note, rating = rating))
                },
                enabled = title.isNotBlank(),
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
