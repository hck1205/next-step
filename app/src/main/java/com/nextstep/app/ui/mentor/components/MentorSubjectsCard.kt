package com.nextstep.app.ui.mentor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag

/** 담당 과목 카드. 아직 고르지 않았으면 안내 문장, 골랐으면 태그 몇 개와 "+n". */
@Composable
internal fun MentorSubjectsCard(subjects: List<SubjectEntity>, needsSetup: Boolean, onChange: () -> Unit) {
    AppCard(onClick = onChange) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("담당 과목", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (needsSetup) {
                    Text("아직 지정하지 않았어요. 눌러서 담당 과목을 고르세요.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        subjects.take(MAX_TAGS).forEach { SubjectTag(it) }
                        if (subjects.size > MAX_TAGS) Text("+${subjects.size - MAX_TAGS}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            TextButton(onClick = onChange) { Text("변경") }
        }
    }
}

private const val MAX_TAGS = 4
