package com.nextstep.app.ui.growth.components

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
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard

/** 신체 기록 한 줄: 날짜 · 키 · 몸무게 · 시력. 누르면 수정, 휴지통은 삭제. */
@Composable
internal fun GrowthRecordRow(record: GrowthRecordEntity, onEdit: () -> Unit, onDelete: (() -> Unit)?) {
    AppCard(onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(DateUtils.formatDate(DateUtils.fromEpochDay(record.date)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    listOfNotNull(record.heightCm?.let { "${growthNumber(it)}cm" }, record.weightKg?.let { "${growthNumber(it)}kg" }, visionText(record.visionLeft, record.visionRight)).joinToString(" · "),
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (record.note.isNotBlank()) Text(record.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "기록 삭제", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
