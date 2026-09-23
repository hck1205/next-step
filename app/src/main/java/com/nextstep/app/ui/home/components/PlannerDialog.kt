package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.planner.PlanOptions
import com.nextstep.app.ui.components.input.TimeField
import java.time.LocalTime
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
internal fun PlannerDialog(defaults: PlanOptions, onDismiss: () -> Unit, onGenerate: (PlanOptions) -> Unit) {
    var days by remember { mutableStateOf(defaults.days.toString()) }
    var start by remember { mutableStateOf(defaults.startTime) }
    var minutes by remember { mutableStateOf(defaults.sessionMinutes.toString()) }
    var perDay by remember { mutableStateOf(defaults.sessionsPerDay.toString()) }
    var weekend by remember { mutableStateOf(defaults.includeWeekend) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("학습 계획 만들기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("우선순위: 밀린 복습 → 멘토 로드맵 → 다음 예습. 이미 있는 일정과 겹치는 시간은 건너뛰어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = days, onValueChange = { days = it.filter { c -> c.isDigit() }.take(2) }, label = { Text("며칠") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = perDay, onValueChange = { perDay = it.filter { c -> c.isDigit() }.take(1) }, label = { Text("하루 회수") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TimeField("시작", start, onChange = { start = it }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = minutes, onValueChange = { minutes = it.filter { c -> c.isDigit() }.take(3) }, label = { Text("1회(분)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("주말 포함", modifier = Modifier.weight(1f))
                    Switch(checked = weekend, onCheckedChange = { weekend = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGenerate(PlanOptions(days = (days.toIntOrNull() ?: 7).coerceIn(1, 30), startTime = start, sessionMinutes = (minutes.toIntOrNull() ?: 50).coerceIn(10, 180), sessionsPerDay = (perDay.toIntOrNull() ?: 2).coerceIn(1, 5), includeWeekend = weekend))
                onDismiss()
            }) { Text("만들기") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
