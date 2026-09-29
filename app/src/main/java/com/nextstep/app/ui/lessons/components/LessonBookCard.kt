package com.nextstep.app.ui.lessons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.lesson.LessonBook
import com.nextstep.app.domain.lesson.LessonStatus
import com.nextstep.app.domain.lesson.Lessons
import com.nextstep.app.ui.components.card.AppCard
import java.time.LocalDate

/** 멘토 한 명의 이 달 수업: 일정 · 요약 · 수업료(가까우면) · 수업 날들. [onMark] 가 있으면(멘토) 출결을 적습니다. */
@Composable
fun LessonBookCard(book: LessonBook, onMark: ((LocalDate, LessonStatus?) -> Unit)?) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(book.mentorName, style = MaterialTheme.typography.titleSmall)
            Text(book.plan.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(book.summary.line, style = MaterialTheme.typography.bodyMedium)
            book.tuition?.let { Lessons.tuitionLine(it, payer = onMark == null) }?.let {
                Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            book.days.forEach { day ->
                HorizontalDivider()
                LessonDayRow(day, onMark?.let { mark -> { s: LessonStatus? -> mark(day.date, s) } })
            }
        }
    }
}
