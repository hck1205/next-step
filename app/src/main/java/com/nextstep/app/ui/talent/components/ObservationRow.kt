package com.nextstep.app.ui.talent.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard

/** 관찰 메모 한 줄: 내용, 날짜 · 남긴 사람, 강도 점. */
@Composable
internal fun ObservationRow(observation: ObservationEntity, onDelete: (() -> Unit)?) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(observation.text, style = MaterialTheme.typography.bodyLarge)
                Text(
                    listOfNotNull(DateUtils.formatDate(DateUtils.fromEpochDay(observation.date)), observation.authorName.takeIf { it.isNotBlank() }, "●".repeat(observation.strength.coerceIn(1, MAX_STRENGTH)))
                        .joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "관찰 삭제", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

private const val MAX_STRENGTH = 3
