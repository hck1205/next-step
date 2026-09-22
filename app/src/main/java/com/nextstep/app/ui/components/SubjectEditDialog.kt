package com.nextstep.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.ui.theme.SubjectPalette

/** 과목 추가/편집 다이얼로그. */
@Composable
fun SubjectEditDialog(existing: SubjectEntity?, onDismiss: () -> Unit, onSave: (String, Long, Int, String) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var color by remember { mutableStateOf(existing?.color ?: SubjectPalette.first()) }
    var goal by remember { mutableStateOf((existing?.weeklyGoalMinutes ?: 180).toString()) }
    var teacher by remember { mutableStateOf(existing?.teacher ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "과목 추가" else "과목 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("과목명") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("담당 선생님 (선택)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = goal, onValueChange = { goal = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("주간 목표 학습 시간(분)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
                )
                Text("색상", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SubjectPalette.forEach { c ->
                        val selected = c == color
                        Box(
                            Modifier.size(26.dp)
                                .border(if (selected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                .padding(if (selected) 4.dp else 1.dp)
                                .background(subjectColor(c), CircleShape)
                                .clickable { color = c },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(name.trim(), color, goal.toIntOrNull() ?: 0, teacher.trim()); onDismiss() }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
