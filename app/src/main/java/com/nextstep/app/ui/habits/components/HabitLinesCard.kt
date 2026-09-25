package com.nextstep.app.ui.habits.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.components.card.AppCard

/** 습관을 문장 몇 줄로. 숫자보다 문장이 먼저입니다. */
@Composable
internal fun HabitLinesCard(lines: List<String>) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("최근 4주 공부 습관", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
