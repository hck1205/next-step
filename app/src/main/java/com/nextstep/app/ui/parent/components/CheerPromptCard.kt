package com.nextstep.app.ui.parent.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nextstep.app.ui.components.card.AppCard

/** 격려 카드: 최근 한마디를 보여 주고, 카드는 격려 화면으로, 버튼은 바로 쓰기로. */
@Composable
internal fun CheerPromptCard(latestText: String?, onOpen: () -> Unit, onWrite: () -> Unit) {
    AppCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("격려", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(latestText?.let { "최근: “$it”" } ?: "오늘 한마디를 남기면 내일 기록 일수가 늘어나요", style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onWrite) { Text("격려하기") }
        }
    }
}
