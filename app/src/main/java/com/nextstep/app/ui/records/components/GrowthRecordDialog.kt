package com.nextstep.app.ui.records.components

import com.nextstep.app.domain.time.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.ui.components.input.DateField
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/** 성장 기록 입력: 날짜, 키, 몸무게, 좌·우 시력, 메모. 아는 값만 넣습니다. */
@Composable
fun GrowthRecordDialog(existing: GrowthRecordEntity?, today: LocalDate, onConfirm: (GrowthRecordEntity) -> Unit, onDismiss: () -> Unit) {
    var date by remember { mutableStateOf(existing?.date?.let { DateUtils.fromEpochDay(it) } ?: today) }
    var height by remember { mutableStateOf(existing?.heightCm?.toString() ?: "") }
    var weight by remember { mutableStateOf(existing?.weightKg?.toString() ?: "") }
    var visionL by remember { mutableStateOf(existing?.visionLeft?.toString() ?: "") }
    var visionR by remember { mutableStateOf(existing?.visionRight?.toString() ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    val anyValue = listOf(height, weight, visionL, visionR).any { it.toDoubleOrNull() != null }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "성장 기록" else "성장 기록 수정") },
        text = {
            Column {
                DateField(label = "측정일", date = date, onChange = { date = it })
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(height, { height = it }, "키 (cm)", Modifier.weight(1f))
                    NumberField(weight, { weight = it }, "몸무게 (kg)", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(visionL, { visionL = it }, "시력 왼쪽", Modifier.weight(1f))
                    NumberField(visionR, { visionR = it }, "시력 오른쪽", Modifier.weight(1f))
                }
                Text("시력은 시력표 기준(예: 1.0, 0.7). 학교·소아과 검진 결과를 그대로 적으면 돼요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("메모 (예: 영유아검진 6차)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val base = existing ?: GrowthRecordEntity(familyId = "", date = date.toEpochDay())
                    onConfirm(base.copy(date = date.toEpochDay(), heightCm = height.toDoubleOrNull(), weightKg = weight.toDoubleOrNull(), visionLeft = visionL.toDoubleOrNull(), visionRight = visionR.toDoubleOrNull(), note = note))
                },
                enabled = anyValue,
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

@Composable
private fun NumberField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = modifier)
}
