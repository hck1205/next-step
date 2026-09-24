package com.nextstep.app.ui.curriculum.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.components.card.AppCard

/** 짧은 문장 목록 카드. 또래가 더 배운 것, 다음 학기 미리 보기에 씁니다. */
@Composable
internal fun BulletCard(lines: List<String>, footnote: String? = null, bullet: String = "• ") {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            lines.forEach { Text(bullet + it, style = MaterialTheme.typography.bodyMedium) }
            if (footnote != null) Text(footnote, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
