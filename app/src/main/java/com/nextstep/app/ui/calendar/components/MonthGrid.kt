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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.calendar.CalendarUiState
import com.nextstep.app.ui.calendar.DayMarker
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.ColorDot
import java.time.DayOfWeek
import java.time.LocalDate

/** 한 달 달력: 달 넘기기 · 요일 · 날짜 칸(일정·시험·할 일·공부 점) · 점 설명. 월요일부터 시작합니다. */
@Composable
internal fun MonthGrid(state: CalendarUiState, onPrev: () -> Unit, onNext: () -> Unit, onSelect: (LocalDate) -> Unit) {
    val month = state.month
    val first = month.atDay(1)
    val leading = (first.dayOfWeek.value - DayOfWeek.MONDAY.value + DAYS_IN_WEEK) % DAYS_IN_WEEK
    val rows = (leading + month.lengthOfMonth() + DAYS_IN_WEEK - 1) / DAYS_IN_WEEK
    val today = DateUtils.today()
    AppCard {
        Column {
            MonthHeader(DateUtils.formatMonth(first), onPrev, onNext)
            WeekdayRow()
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (c in 0 until DAYS_IN_WEEK) {
                        val day = r * DAYS_IN_WEEK + c - leading + 1
                        if (day < 1 || day > month.lengthOfMonth()) {
                            Spacer(Modifier.weight(1f).height(CELL_HEIGHT.dp))
                        } else {
                            val date = month.atDay(day)
                            DayCell(date, state.markers[date], selected = date == state.selected, isToday = date == today, onClick = { onSelect(date) }, modifier = Modifier.weight(1f))
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

@Composable
private fun MonthHeader(title: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달") }
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        IconButton(onClick = onNext) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달") }
    }
}

@Composable
private fun WeekdayRow() {
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

/** 날짜 한 칸: 숫자(오늘은 굵게, 일요일은 빨강) 아래에 그날의 점. */
@Composable
private fun DayCell(date: LocalDate, marker: DayMarker?, selected: Boolean, isToday: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier.height(CELL_HEIGHT.dp).padding(2.dp)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(4.dp))
        Text(
            date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                selected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.primary
                date.dayOfWeek == DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
        if (marker != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 2.dp)) {
                if (marker.hasExam) ColorDot(MaterialTheme.colorScheme.error, DOT_SIZE)
                else if (marker.hasEvent) ColorDot(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, DOT_SIZE)
                if (marker.hasTask) ColorDot(MaterialTheme.colorScheme.tertiary, DOT_SIZE)
                if (marker.studyMinutes > 0) ColorDot(MaterialTheme.colorScheme.secondary, DOT_SIZE)
            }
        }
    }
}

private const val DAYS_IN_WEEK = 7
private const val CELL_HEIGHT = 48
private const val DOT_SIZE = 5
