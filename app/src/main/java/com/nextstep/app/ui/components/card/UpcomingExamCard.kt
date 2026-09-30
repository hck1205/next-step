package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.nextstep.app.domain.stats.UpcomingExam
import com.nextstep.app.domain.time.DateUtils

/** 다가오는 시험 하나와 D-day. 학생 홈과 학부모 첫 화면이 같이 씁니다. */
@Composable
fun UpcomingExamCard(exam: UpcomingExam) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("다가오는 시험", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(exam.title, style = MaterialTheme.typography.titleMedium)
                Text(DateUtils.formatFullDate(exam.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(DateUtils.dDay(exam.date), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
    }
}
