package com.nextstep.app.ui.lessons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.lesson.LessonDay
import com.nextstep.app.domain.lesson.LessonStatus
import com.nextstep.app.domain.time.DateUtils

/** 수업 한 날: "3/6 (화)" + 출결. 멘토([onMark])는 출석·결석·보강 필요를 고르고, 고른 것을 다시 누르면 지웁니다. 학부모는 보기만. */
@Composable
fun LessonDayRow(day: LessonDay, onMark: ((LessonStatus?) -> Unit)?, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${day.date.monthValue}/${day.date.dayOfMonth} (${DateUtils.dayOfWeekLabel(day.date.dayOfWeek)})", style = MaterialTheme.typography.bodyMedium)
            if (day.extra) Text("추가 수업", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            if (onMark == null) {
                Text(day.status?.label ?: "—", style = MaterialTheme.typography.bodyMedium, color = statusColor(day.status))
            }
        }
        if (onMark != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LessonStatus.entries.forEach { s ->
                    FilterChip(selected = day.status == s, onClick = { onMark(if (day.status == s) null else s) }, label = { Text(s.label) })
                }
            }
        }
    }
}

@Composable
private fun statusColor(status: LessonStatus?) = when (status) {
    LessonStatus.DONE -> MaterialTheme.colorScheme.primary
    LessonStatus.ABSENT, LessonStatus.MAKEUP -> MaterialTheme.colorScheme.error
    null -> MaterialTheme.colorScheme.onSurfaceVariant
}
