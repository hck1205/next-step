package com.nextstep.app.ui.assignments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.mentor.AssignmentSubject
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress
import com.nextstep.app.ui.components.card.subjectColor

/** 과목별 과제 완료율(낮은 과목 먼저). */
@Composable
internal fun AssignmentSubjectsCard(bySubject: List<AssignmentSubject>) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("과목별 완료", style = MaterialTheme.typography.titleSmall)
            bySubject.forEach { s ->
                LabeledProgress(
                    label = s.subject?.name ?: "과목 없음",
                    ratio = if (s.total == 0) 0f else s.done.toFloat() / s.total,
                    color = s.subject?.let { subjectColor(it.color) } ?: MaterialTheme.colorScheme.outline,
                    trailing = "${s.done}/${s.total}",
                )
            }
        }
    }
}
