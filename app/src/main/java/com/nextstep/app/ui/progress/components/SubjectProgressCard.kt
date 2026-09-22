package com.nextstep.app.ui.progress.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.ColorDot
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.components.subjectColor

@Composable
internal fun SubjectProgressCard(p: SubjectProgress, onClick: () -> Unit) {
    val color = subjectColor(p.subject.color)
    AppCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorDot(color, 12)
                Spacer(Modifier.width(8.dp))
                Text(p.subject.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (p.subject.weeklyGoalMinutes > 0) Text("주 ${DateUtils.formatMinutes(p.subject.weeklyGoalMinutes)} 목표", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (p.total == 0) {
                Text("등록된 단원이 없어요. 눌러서 추가하세요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LabeledProgress("학급 진도", p.classRatio, color.copy(alpha = 0.5f), trailing = "${p.classCovered}/${p.total} 단원")
                LabeledProgress("내 복습", p.myRatio, color, trailing = "${p.reviewed}/${p.total} 단원")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QueueChip("복습 대기 ${p.reviewQueue.size + (p.classCovered - p.reviewed - p.reviewQueue.size).coerceAtLeast(0)}", MaterialTheme.colorScheme.tertiary)
                    QueueChip("예습 추천 ${p.previewQueue.size}", MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
