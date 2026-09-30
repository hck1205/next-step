package com.nextstep.app.ui.mentor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.lesson.LessonBook
import com.nextstep.app.domain.lesson.LessonDay
import com.nextstep.app.domain.lesson.LessonPlan
import com.nextstep.app.domain.lesson.LessonStatus
import com.nextstep.app.domain.lesson.Lessons
import com.nextstep.app.ui.components.dialog.LessonPlanDialog
import java.time.LocalDate

/** 멘토 오늘 카드 "수업·출결": 오늘 수업 출결(한 번 누르기) · 이 달 요약 · 수업료 받을 날. 일정이 없으면 정하기부터. */
@Composable
internal fun LessonTodayCard(book: LessonBook?, today: LessonDay?, plan: LessonPlan, onMark: (LocalDate, LessonStatus?) -> Unit, onSavePlan: (LessonPlan) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!plan.isSet) {
            Text("수업 요일·시간·수업료를 정하면 출결과 수업료 받을 날을 챙겨 드려요", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { editing = true }) { Text("수업 일정 정하기") }
        } else {
            Text(plan.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TodayLesson(today, onMark)
            book?.let { Text(it.summary.line, style = MaterialTheme.typography.bodyMedium) }
            book?.tuition?.let { Lessons.tuitionLine(it) }?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
            TextButton(onClick = { editing = true }) { Text("일정 바꾸기") }
        }
    }
    if (editing) LessonPlanDialog(plan, onDismiss = { editing = false }, onSave = onSavePlan)
}

@Composable
private fun TodayLesson(today: LessonDay?, onMark: (LocalDate, LessonStatus?) -> Unit) {
    if (today == null) {
        Text("오늘은 수업이 없어요", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Text("오늘 수업", style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        LessonStatus.entries.forEach { s ->
            FilterChip(selected = today.status == s, onClick = { onMark(today.date, if (today.status == s) null else s) }, label = { Text(s.label) })
        }
    }
}
