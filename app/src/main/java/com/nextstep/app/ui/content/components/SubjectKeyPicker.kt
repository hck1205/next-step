package com.nextstep.app.ui.content.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 과목 키 선택: 기존 키 칩 + 직접 입력. */
@Composable
internal fun SubjectKeyPicker(keys: List<String>, value: String, onChange: (String) -> Unit) {
    Column {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            keys.forEach { k -> FilterChip(selected = value == k, onClick = { onChange(if (value == k) "" else k) }, label = { Text(k) }) }
        }
        OutlinedTextField(value = value, onValueChange = onChange, label = { Text("과목") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    }
}
