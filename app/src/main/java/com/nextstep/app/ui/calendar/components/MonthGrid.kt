package com.nextstep.app.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.calendar.CalendarUiState
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.ColorDot
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
internal fun MonthGrid(state: CalendarUiState, onPrev: () -> Unit, onNext: () -> Unit, onSelect: (LocalDate) -> Unit) {
    val month = state.month
    val first = month.atDay(1)
    val leading = (first.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val cells = leading + month.lengthOfMonth()
    val rows = (cells + 6) / 7
    val today = DateUtils.today()
    AppCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrev) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달") }
                Text(DateUtils.formatMonth(first), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                IconButton(onClick = onNext) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달") }
            }
            Row(Modifier.fillMaxWidth()) {
                DayOfWeek.entries.forEach { d ->
                    Text(
                        DateUtils.dayOfWeekLabel(d), style = MaterialTheme.typography.labelSmall,
                        color = when (d) { DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error; DayOfWeek.SATURDAY -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.onSurfaceVariant },
                        modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        val idx = r * 7 + c
                        val day = idx - leading + 1
                        if (day < 1 || day > month.lengthOfMonth()) {
                            Spacer(Modifier.weight(1f).height(48.dp))
                        } else {
                            val date = month.atDay(day)
                            val marker = state.markers[date]
                            val selected = date == state.selected
                            Column(
                                Modifier.weight(1f).height(48.dp).padding(2.dp)
                                    .background(if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent, MaterialTheme.shapes.small)
                                    .clickable { onSelect(date) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    day.toString(), style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        selected -> MaterialTheme.colorScheme.onPrimary
                                        date == today -> MaterialTheme.colorScheme.primary
                                        date.dayOfWeek == DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                )
                                if (marker != null) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 2.dp)) {
                                        if (marker.hasExam) ColorDot(MaterialTheme.colorScheme.error, 5)
                                        else if (marker.hasEvent) ColorDot(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, 5)
                                        if (marker.hasTask) ColorDot(MaterialTheme.colorScheme.tertiary, 5)
                                        if (marker.studyMinutes > 0) ColorDot(MaterialTheme.colorScheme.secondary, 5)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LegendItem("일정", MaterialTheme.colorScheme.primary)
                LegendItem("시험", MaterialTheme.colorScheme.error)
                LegendItem("할 일", MaterialTheme.colorScheme.tertiary)
                LegendItem("학습 기록", MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
