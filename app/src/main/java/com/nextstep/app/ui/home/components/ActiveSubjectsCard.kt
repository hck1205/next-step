package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag

/** 지금 배우는 과목들과 다음 단원. 과목당 한 줄. */
@Composable
internal fun ActiveSubjectsCard(subjects: List<SubjectProgress>, onOpenSubject: (String) -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            subjects.forEach { p ->
                val current = p.reviewQueue.firstOrNull() ?: p.previewQueue.firstOrNull()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectTag(p.subject)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(current?.title ?: "다음 단원 없음", style = MaterialTheme.typography.bodyMedium)
                        Text("학급 ${p.classCovered}/${p.total} 단원 · 복습 ${p.reviewed}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { onOpenSubject(p.subject.id) }) { Text("열기") }
                }
            }
        }
    }
}
