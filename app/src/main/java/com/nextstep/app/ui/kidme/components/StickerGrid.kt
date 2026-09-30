package com.nextstep.app.ui.kidme.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.StickerDay
import java.time.LocalDate

/** 4주 스티커판: 한 줄에 7일. 공부한 날 별, 활동한 날 꽃, 빈 날은 날짜만. 오늘은 테두리. */
@Composable
internal fun StickerGrid(days: List<StickerDay>, today: LocalDate) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        days.chunked(WEEK).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                week.forEach { day ->
                    val label = when {
                        day.studied -> "${day.date.dayOfMonth}일 별"
                        day.didActivity -> "${day.date.dayOfMonth}일 꽃"
                        else -> "${day.date.dayOfMonth}일"
                    }
                    Box(
                        Modifier.weight(1f).aspectRatio(1f)
                            .border(if (day.date == today) 2.dp else 1.dp, if (day.date == today) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, CircleShape)
                            .semantics { contentDescription = label },
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            day.studied -> Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            day.didActivity -> Icon(Icons.Default.LocalFlorist, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            else -> Text("${day.date.dayOfMonth}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private const val WEEK = 7
