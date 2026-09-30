package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** 다른 화면으로 가는 카드: 제목·설명·오른쪽 텍스트 버튼. 카드 전체와 버튼 모두 [onClick]. */
@Composable
fun LinkCard(title: String, description: String, onClick: () -> Unit, actionLabel: String = "열기", titleColor: Color = Color.Unspecified) {
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = titleColor)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onClick) { Text(actionLabel) }
        }
    }
}
