package com.nextstep.app.ui.grades.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag
import com.nextstep.app.ui.common.oneDecimal

@Composable
internal fun GradeRow(g: GradeEntity, subjects: List<SubjectEntity>, onClick: () -> Unit) {
    val subject = subjects.firstOrNull { it.id == g.subjectId }
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(g.title, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SubjectTag(subject)
                    Text(g.examType.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(DateUtils.formatDate(DateUtils.fromEpochDay(g.date)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${g.score.oneDecimal()} / ${g.maxScore.oneDecimal()}",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    color = when {
                        g.percent >= 90 -> MaterialTheme.colorScheme.secondary
                        g.percent < 70 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
                g.classAverage?.let { avg ->
                    val diff = g.score - avg
                    Text(
                        "반 평균 ${avg.oneDecimal()} (${if (diff >= 0) "+" else ""}${diff.oneDecimal()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (diff >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
