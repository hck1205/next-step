package com.nextstep.app.ui.goal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.components.card.AppCard

/** 목표를 이룬 뒤: 더 큰 목표([parentTitle])로 이어지는 다음 목표를 만들자고 권합니다. */
@Composable
internal fun NextGoalCard(parentTitle: String?, onAdd: () -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("달성했어요! 다음 목표로 이어 갈까요?", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onAdd) { Text(parentTitle?.let { "\"$it\"로 이어지는 다음 목표 만들기" } ?: "다음 목표 만들기") }
        }
    }
}
