package com.nextstep.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.insight.Insight
import com.nextstep.app.domain.insight.InsightAction
import com.nextstep.app.domain.insight.InsightKind

@Composable
fun InsightCard(insight: Insight, subjects: List<SubjectEntity>, onAction: ((InsightAction) -> Unit)?) {
    val (icon, color) = when (insight.kind) {
        InsightKind.STRENGTH -> Icons.Default.ThumbUp to MaterialTheme.colorScheme.secondary
        InsightKind.WEAKNESS -> Icons.AutoMirrored.Filled.TrendingDown to MaterialTheme.colorScheme.error
        InsightKind.SUGGESTION -> Icons.Default.Lightbulb to MaterialTheme.colorScheme.primary
        InsightKind.ALERT -> Icons.Default.Warning to MaterialTheme.colorScheme.tertiary
    }
    val subject = subjects.firstOrNull { it.id == insight.subjectId }
    AppCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.background(color.copy(alpha = 0.12f), MaterialTheme.shapes.small).padding(8.dp)) {
                Icon(icon, contentDescription = insight.kind.label, tint = color)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(insight.kind.label, style = MaterialTheme.typography.labelSmall, color = color)
                    if (subject != null) SubjectTag(subject)
                }
                Text(insight.title, style = MaterialTheme.typography.titleSmall)
                Text(insight.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val action = insight.action
                if (action != null && onAction != null) {
                    TextButton(onClick = { onAction(action) }, contentPadding = PaddingValues(0.dp)) {
                        Text(
                            when (action) { is InsightAction.CreateTask -> "'${action.title}' 할 일로 추가" },
                        )
                    }
                }
            }
        }
    }
}
