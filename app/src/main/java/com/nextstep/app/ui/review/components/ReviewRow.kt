package com.nextstep.app.ui.review.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.ReviewItem
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag

/** 복습 한 줄: 과목 · 단원 · 이해도. 오른쪽에 "할 일로"(쓸 수 있는 사람만)와 "했어요"(학생만). */
@Composable
internal fun ReviewRow(item: ReviewItem, onAddTask: (() -> Unit)?, onDone: (() -> Unit)?) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SubjectTag(item.subject)
                Text(item.topic.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    item.topic.status.label + if (item.topic.confidence > 0) " · 이해도 ${item.topic.confidence}%" else "",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            onAddTask?.let { IconButton(onClick = it) { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "오늘 할 일로") } }
            onDone?.let { IconButton(onClick = it) { Icon(Icons.Default.CheckCircle, contentDescription = "했어요", tint = MaterialTheme.colorScheme.primary) } }
        }
    }
}
