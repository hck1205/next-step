package com.nextstep.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.grades.components.Double
import java.time.LocalDate

@Composable
fun GradeEditDialog(
    existing: GradeEntity?,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (String, String, ExamType, Double, Double, Double?, LocalDate, String) -> Unit,
) {
    var subjectId by remember { mutableStateOf(existing?.subjectId ?: subjects.firstOrNull()?.id ?: "") }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var type by remember { mutableStateOf(existing?.examType ?: ExamType.QUIZ) }
    var score by remember { mutableStateOf(existing?.score?.trim() ?: "") }
    var max by remember { mutableStateOf(existing?.maxScore?.trim() ?: "100") }
    var classAvg by remember { mutableStateOf(existing?.classAverage?.trim() ?: "") }
    var date by remember { mutableStateOf(existing?.let { DateUtils.fromEpochDay(it.date) } ?: LocalDate.now()) }
    var memo by remember { mutableStateOf(existing?.memo ?: "") }
    val valid = subjectId.isNotBlank() && title.isNotBlank() && score.toDoubleOrNull() != null && (max.toDoubleOrNull() ?: 0.0) > 0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "성적 추가" else "성적 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SubjectPicker(subjects, subjectId, onSelect = { it?.let { id -> subjectId = id } }, allowNone = false)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("시험명 (예: 1학기 중간고사)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OptionPicker(ExamType.entries, type, label = { it.label }, onSelect = { type = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = score, onValueChange = { score = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("점수") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = max, onValueChange = { max = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("만점") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = classAvg, onValueChange = { classAvg = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("반 평균 (선택)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                DateField("시험일", date, onChange = { date = it })
                OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모 (틀린 유형 등)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                onSave(subjectId, title.trim(), type, score.toDouble(), max.toDouble(), classAvg.toDoubleOrNull(), date, memo.trim()); onDismiss()
            }) { Text("저장") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = { onDelete(); onDismiss() }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        },
    )
}
