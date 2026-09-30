package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.nextstep.app.domain.time.DateUtils
import java.time.DayOfWeek

/** 달력의 요일 줄(월요일부터, 일요일 빨강 · 토요일 브랜드 색). */
@Composable
fun WeekdayRow() {
    Row(Modifier.fillMaxWidth()) {
        DayOfWeek.entries.forEach { d ->
            Text(
                DateUtils.dayOfWeekLabel(d), style = MaterialTheme.typography.labelSmall,
                color = when (d) { DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error; DayOfWeek.SATURDAY -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.onSurfaceVariant },
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
            )
        }
    }
}
