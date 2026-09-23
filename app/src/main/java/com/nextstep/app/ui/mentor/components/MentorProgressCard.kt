package com.nextstep.app.ui.mentor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.ui.common.ratio
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.ColorDot
import com.nextstep.app.ui.components.card.LabeledProgress
import com.nextstep.app.ui.components.card.subjectColor

/** 과목 하나의 학급 진도 대비 복습률. 눌러서 단원 관리로. */
@Composable
internal fun MentorProgressCard(p: SubjectProgress, onOpen: () -> Unit) {
    val color = subjectColor(p.subject.color)
    AppCard(onClick = onOpen) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorDot(color, 10)
                Spacer(Modifier.width(8.dp))
                Text(p.subject.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("학급 ${p.classCovered}/${p.total} · 복습 ${p.reviewed}/${p.total}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LabeledProgress("복습률", ratio(p.reviewed, p.classCovered), color)
            if (p.reviewQueue.isNotEmpty()) Text("복습 필요: ${p.reviewQueue.joinToString { it.title }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}
