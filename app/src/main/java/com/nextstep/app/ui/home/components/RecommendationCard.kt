package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.nextstep.app.domain.content.ContentRecommendation
import com.nextstep.app.ui.components.card.AppCard

/** 오늘의 추천 영상 하나. 카드는 링크 열기, 버튼은 "봤어요". */
@Composable
internal fun RecommendationCard(rec: ContentRecommendation, onOpen: () -> Unit, onWatched: () -> Unit) {
    val c = rec.content
    AppCard(onClick = onOpen) {
        Column {
            Text(rec.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(c.title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(listOf(c.channel, c.subjectKey, c.contentType.label).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                TextButton(onClick = onWatched) { Text("봤어요") }
            }
        }
    }
}
