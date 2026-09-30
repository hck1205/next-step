package com.nextstep.app.ui.components.input

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 제목([title]) 아래 고르기 칩 한 줄. 넘치면 옆으로 밉니다. 하나 고르기·여러 개 고르기 모두 [selected] 로 표시합니다. */
@Composable
fun <T> ChipRow(options: List<T>, selected: (T) -> Boolean, label: (T) -> String, onClick: (T) -> Unit, modifier: Modifier = Modifier, title: String? = null) {
    Column(modifier) {
        if (title != null) Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { o -> FilterChip(selected = selected(o), onClick = { onClick(o) }, label = { Text(label(o)) }) }
        }
    }
}
