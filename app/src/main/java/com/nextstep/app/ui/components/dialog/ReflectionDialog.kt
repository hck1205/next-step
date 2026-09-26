package com.nextstep.app.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.selfdirection.ReflectionForm
import com.nextstep.app.ui.components.icon.moodIcon
import com.nextstep.app.ui.components.icon.moodLabel

/**
 * 주간 돌아보기. 어린 단계는 기분 얼굴만([ReflectionForm.FACES]), 다음은 제일 좋았던 것 한 줄,
 * 초3부터는 잘된 것 · 어려웠던 것 · 다음 주에 바꿀 것(바꿀 것은 다음 계획 창에 다시 보입니다).
 */
@Composable
fun ReflectionDialog(form: ReflectionForm, title: String, onDismiss: () -> Unit, onSave: (mood: Int, good: String, hard: String, change: String) -> Unit) {
    var mood by remember { mutableIntStateOf(0) }
    var good by remember { mutableStateOf("") }
    var hard by remember { mutableStateOf("") }
    var change by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("이번 주 어땠어요?", style = MaterialTheme.typography.bodyMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    (1..MOODS).forEach { m ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconToggleButton(checked = mood == m, onCheckedChange = { mood = m }) {
                                Icon(moodIcon(m), contentDescription = moodLabel(m), modifier = Modifier.size(40.dp),
                                    tint = if (mood == m) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                            }
                            Text(moodLabel(m), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                if (form != ReflectionForm.FACES) {
                    OutlinedTextField(good, { good = it }, label = { Text(if (form == ReflectionForm.FACE_AND_BEST) "제일 좋았던 것" else "잘된 것") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                if (form == ReflectionForm.THREE_LINES) {
                    OutlinedTextField(hard, { hard = it }, label = { Text("어려웠던 것") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(change, { change = it }, label = { Text("다음 주에 바꿀 것") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(mood, good, hard, change) }, enabled = mood > 0) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

private const val MOODS = 3
