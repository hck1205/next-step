package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.input.ChipRow

/** 하나 고르기 단계: 제목 · 안내 · 고르기 칩(다시 누르면 비움) · 직접 적기. 비워 두고 넘어가도 됩니다. */
@Composable
internal fun ChoiceStep(title: String, hint: String, ideas: List<String>, value: String, onChange: (String) -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (ideas.isNotEmpty()) ChipRow(ideas, selected = { it == value }, label = { it }, onClick = { onChange(if (it == value) "" else it) })
            OutlinedTextField(value, onChange, label = { Text("직접 적기") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
    }
}
