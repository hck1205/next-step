package com.nextstep.app.ui.kidfamily.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.access.ParentGate

/** 어른 확인: 곱셈 하나를 맞히면 설정이 열립니다. 틀리면 새 문제가 나옵니다. */
@Composable
internal fun ParentGateDialog(gate: ParentGate, wrong: Boolean, onAnswer: (String) -> Unit, onDismiss: () -> Unit) {
    var answer by remember(gate) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("어른만 열 수 있어요") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("부모님께 보여 주세요. ${gate.question}", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(value = answer, onValueChange = { answer = it.filter(Char::isDigit) }, singleLine = true, label = { Text("답") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                if (wrong) Text("다시 해 볼까요? 새 문제예요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = { onAnswer(answer) }) { Text("확인") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("닫기") } },
    )
}
