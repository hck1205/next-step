package com.nextstep.app.ui.content.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.model.ContentScope
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.components.AppCard

@Composable
internal fun ContentRow(c: ContentEntity, reason: String?, caps: Capabilities, onOpen: () -> Unit, onRate: (Int) -> Unit, onWatched: (Boolean) -> Unit, onEdit: () -> Unit) {
    AppCard(onClick = onOpen) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (reason != null) Text(reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.title.ifBlank { c.url }, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOf(c.channel, c.subjectKey, c.gradeLevel.takeIf { it != GradeLevel.ALL }?.label, c.contentType.label, c.durationMinutes.takeIf { it > 0 }?.let { "${it}분" })
                            .filterNotNull().filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (c.scope == ContentScope.GLOBAL) Text("공용", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            }
            if (c.summary.isNotBlank()) Text(c.summary, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (c.keywordList.isNotEmpty()) Text(c.keywordList.joinToString("  ") { "#$it" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingStars(c.averageRating, enabled = c.scope == ContentScope.FAMILY, onRate = onRate)
                if (c.ratingCount > 0) Text(" ${c.ratingCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                if (caps.isStudent) TextButton(onClick = { onWatched(!c.watched) }) { Text(if (c.watched) "다시 보기" else "봤어요") }
                TextButton(onClick = onEdit) { Text("정보") }
            }
        }
    }
}
