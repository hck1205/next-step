package com.nextstep.app.ui.goal.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.input.DateField
import java.time.LocalDate

/** 목표 고치기: 제목 · 이유 · 기한. */
@Composable
internal fun EditGoalDialog(goal: GoalEntity, today: LocalDate, onDismiss: () -> Unit, onSave: (String, String, LocalDate?) -> Unit) {
    var title by remember { mutableStateOf(goal.title) }
    var why by remember { mutableStateOf(goal.description) }
    var hasDate by remember { mutableStateOf(goal.targetDate != null) }
    var date by remember { mutableStateOf(goal.targetDate?.let { DateUtils.fromEpochDay(it) } ?: today.plusMonths(1)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("목표 고치기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("목표") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(why, { why = it }, label = { Text("왜 하나요") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("기한", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(checked = hasDate, onCheckedChange = { hasDate = it })
                }
                if (hasDate) DateField("기한", date, onChange = { date = it })
            }
        },
        confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onSave(title, why, date.takeIf { hasDate }); onDismiss() }) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
