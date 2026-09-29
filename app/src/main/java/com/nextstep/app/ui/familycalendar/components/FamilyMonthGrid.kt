package com.nextstep.app.ui.familycalendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.ColorDot
import com.nextstep.app.ui.components.icon.familyKindColor
import com.nextstep.app.ui.components.layout.MonthHeader
import com.nextstep.app.ui.components.layout.WeekdayRow
import com.nextstep.app.ui.familycalendar.FamilyCalendarUiState
import java.time.DayOfWeek
import java.time.LocalDate

/** 가족 달력의 한 달 칸: 날마다 가족 일정 종류의 색 점(세 개까지)과, 아이 공부 일정이 있으면 회색 점. 월요일부터. */
@Composable
internal fun FamilyMonthGrid(state: FamilyCalendarUiState, onPrev: () -> Unit, onNext: () -> Unit, onDay: (LocalDate) -> Unit) {
    val first = state.month.atDay(1)
    val leading = (first.dayOfWeek.value - DayOfWeek.MONDAY.value + DAYS_IN_WEEK) % DAYS_IN_WEEK
    val rows = (leading + state.month.lengthOfMonth() + DAYS_IN_WEEK - 1) / DAYS_IN_WEEK
    AppCard {
        Column {
            MonthHeader(DateUtils.formatMonth(first), onPrev, onNext)
            WeekdayRow()
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (c in 0 until DAYS_IN_WEEK) {
                        val day = r * DAYS_IN_WEEK + c - leading + 1
                        if (day < 1 || day > state.month.lengthOfMonth()) Spacer(Modifier.weight(1f).height(CELL_HEIGHT.dp))
                        else {
                            val date = state.month.atDay(day)
                            DayCell(date, state.days[date].orEmpty(), date in state.studyDays, date == state.selected, date == state.today, { onDay(date) }, Modifier.weight(1f))
                        }
                    }
                }
            }
            Text("고른 날을 한 번 더 누르면 일정을 넣어요", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** 날짜 한 칸: 숫자(오늘은 굵게, 일요일은 빨강) 아래에 그날의 종류 점. */
@Composable
private fun DayCell(date: LocalDate, items: List<FamilyOccurrence>, hasStudy: Boolean, selected: Boolean, isToday: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier.height(CELL_HEIGHT.dp).padding(2.dp)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, MaterialTheme.shapes.small)
            .clickable(onClickLabel = DateUtils.formatFullDate(date), onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(4.dp))
        Text(
            date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || selected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isToday -> MaterialTheme.colorScheme.primary
                date.dayOfWeek == DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 2.dp)) {
            items.map { it.kind }.distinct().take(MAX_DOTS).forEach { ColorDot(familyKindColor(it), DOT_SIZE) }
            if (hasStudy) ColorDot(MaterialTheme.colorScheme.outline, DOT_SIZE)
        }
    }
}

private const val DAYS_IN_WEEK = 7
private const val CELL_HEIGHT = 52
private const val DOT_SIZE = 6
private const val MAX_DOTS = 3
