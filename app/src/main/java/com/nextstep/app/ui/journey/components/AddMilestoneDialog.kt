package com.nextstep.app.ui.journey.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.journey.MilestoneCategory
import com.nextstep.app.ui.components.DateField
import com.nextstep.app.ui.components.OptionPicker
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/** 직접 추가하는 이정표 입력 대화상자. 제목·설명·종류·마감일·준비 시작 시점(개월)을 받습니다. */
@Composable
fun AddMilestoneDialog(
    today: LocalDate,
    onConfirm: (title: String, description: String, category: MilestoneCategory, dueDate: LocalDate, leadMonths: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MilestoneCategory.ADMIN) }
    var dueDate by remember { mutableStateOf(today.plusMonths(1)) }
    var leadMonths by remember { mutableStateOf(1) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("이정표 추가") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목 (예: 영어유치원 설명회)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("메모") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OptionPicker(options = MilestoneCategory.entries, selected = category, label = { it.label }, onSelect = { category = it })
                Spacer(Modifier.height(8.dp))
                DateField(label = "마감일", date = dueDate, onChange = { dueDate = it })
                Spacer(Modifier.height(8.dp))
                OptionPicker(options = LEAD_OPTIONS, selected = leadMonths, label = { if (it == 0) "마감일에 알림" else "${it}개월 전부터 준비" }, onSelect = { leadMonths = it })
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(title, description, category, dueDate, leadMonths) }, enabled = title.isNotBlank()) { Text("추가") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

private val LEAD_OPTIONS = listOf(0, 1, 2, 3, 6, 12)
