package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.input.DateField
import java.time.LocalDate

/** 새 자녀 공간 만들기: 이름과 생년월일 두 가지만. */
@Composable
internal fun AddChildDialog(onConfirm: (String, LocalDate?) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var birth by remember { mutableStateOf(DateUtils.today().minusYears(DEFAULT_AGE_YEARS)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("자녀 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                DateField(label = "생년월일", date = birth, onChange = { birth = it })
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name.trim(), birth); onDismiss() }) { Text("추가") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

private const val DEFAULT_AGE_YEARS = 3L
