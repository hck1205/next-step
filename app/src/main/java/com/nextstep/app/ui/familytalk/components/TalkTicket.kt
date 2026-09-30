package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 다음 주 기다리는 일 한 장을 입장권처럼: 왼쪽에 일정 · 날, 절취선 너머에 D-날과 종류. */
@Composable
internal fun TalkTicket(next: FamilyOccurrence, today: LocalDate) {
    val cs = MaterialTheme.colorScheme
    val days = ChronoUnit.DAYS.between(today, next.date)
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).background(cs.tertiaryContainer, RoundedCornerShape(16.dp))) {
        Column(Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.LocalActivity, contentDescription = null, tint = cs.onTertiaryContainer, modifier = Modifier.size(14.dp))
                Text("다음 주 기다리는 일", style = MaterialTheme.typography.labelMedium, color = cs.onTertiaryContainer)
            }
            Text(next.event.title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Text(FamilyCalendar.dayLabel(next.date, today), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
        Canvas(Modifier.width(2.dp).fillMaxHeight()) {
            drawLine(cs.surface, Offset(0f, 0f), Offset(0f, size.height), 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
        }
        Column(Modifier.padding(horizontal = 16.dp).fillMaxHeight(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (days <= 0) "오늘" else "D-$days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = cs.tertiary)
            Text(next.kind.label, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        }
    }
}
