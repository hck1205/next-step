package com.nextstep.app.ui.mentor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag

/** 최근 성적 한 줄: 제목, 과목 태그, 시험 종류·날짜, 점수. */
@Composable
internal fun MentorGradeRow(grade: GradeEntity, subject: SubjectEntity?) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(grade.title, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    SubjectTag(subject)
                    Text("${grade.examType.label} · ${DateUtils.formatDate(DateUtils.fromEpochDay(grade.date))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("${grade.percent.toInt()}점", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
