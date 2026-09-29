package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.familytalk.FamilyTalk
import com.nextstep.app.domain.familytalk.WeekHighlights
import com.nextstep.app.ui.components.card.AppCard

/** 1단계 ✨ 이번 주 반짝인 순간: 좋았던 것만. 없으면 "쉬어 간 주"라고 다독입니다(밀린 것은 말하지 않음). */
@Composable
internal fun SparkleStep(highlights: WeekHighlights, studentName: String) {
    val lines = FamilyTalk.highlightLines(highlights)
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("✨ ${studentName.ifBlank { "우리 아이" }}의 이번 주 반짝인 순간", style = MaterialTheme.typography.titleMedium)
            if (lines.isEmpty()) {
                Text("이번 주는 쉬어 간 주예요. 쉬는 것도 다음 주를 위한 힘이 돼요 🌙", style = MaterialTheme.typography.bodyLarge)
            } else {
                lines.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
            }
            Text("잘한 것을 하나씩 소리 내어 읽어 주세요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
