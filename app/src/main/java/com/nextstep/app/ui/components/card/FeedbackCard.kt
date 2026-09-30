package com.nextstep.app.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.feedback.FeedbackLine

/**
 * 이번 주 피드백: 줄마다 사실 한 줄(잘한 것은 브랜드 색 점, 챙길 것은 주황 점)과 그 사람이 할 한 걸음.
 * [echo] 는 학부모에게 "아이에게는 이렇게 말해 줬어요"(FeedbackVoice.echo) — 같은 이야기를 하게 돕습니다.
 * [compact] 면 한 걸음 문장을 숨깁니다.
 */
@Composable
fun FeedbackCard(lines: List<FeedbackLine>, echo: String? = null, compact: Boolean = false) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            lines.forEach { line -> FeedbackRow(line, compact) }
            if (echo != null && !compact) {
                HorizontalDivider()
                Text(echo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FeedbackRow(line: FeedbackLine, compact: Boolean) {
    val dot = if (line.good) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.padding(top = 6.dp).size(8.dp).background(dot, CircleShape))
        Column {
            Text(line.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (!compact) Text(line.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
