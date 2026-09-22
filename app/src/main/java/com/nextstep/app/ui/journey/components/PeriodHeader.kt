package com.nextstep.app.ui.journey.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.journey.JourneyPeriod
import com.nextstep.app.domain.time.DateUtils

/** 타임라인 구간 머리글: 구간 이름, 기간, 현재 표시. */
@Composable
fun PeriodHeader(period: JourneyPeriod, isCurrent: Boolean, isPast: Boolean, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                period.label, style = MaterialTheme.typography.titleMedium, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = when { isCurrent -> MaterialTheme.colorScheme.primary; isPast -> MaterialTheme.colorScheme.onSurfaceVariant; else -> MaterialTheme.colorScheme.onSurface },
            )
            Text("${DateUtils.formatDate(period.start)} ~ ${DateUtils.formatDate(period.end)} · ${period.stage.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isCurrent) AssistChip(onClick = {}, label = { Text("지금") })
    }
}
