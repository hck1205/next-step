package com.nextstep.app.ui.roadmap.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** "완료 n개" 제목과 펼치기 스위치. */
@Composable
internal fun DoneToggleRow(count: Int, shown: Boolean, onToggle: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("완료 ${count}개", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Switch(checked = shown, onCheckedChange = onToggle)
    }
}
