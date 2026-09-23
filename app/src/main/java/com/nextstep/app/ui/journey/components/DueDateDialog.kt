package com.nextstep.app.ui.journey.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.ui.components.input.DateField
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
internal fun DueDateDialog(item: JourneyItem, onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    var date by remember { mutableStateOf(item.dueDate) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("마감일 변경") },
        text = { DateField(label = "마감일", date = date, onChange = { date = it }) },
        confirmButton = { TextButton(onClick = { onConfirm(date) }) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
