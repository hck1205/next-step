package com.nextstep.app.ui.assignments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.mentor.AssignmentReport
import com.nextstep.app.ui.components.card.AppCard

/** 과제 요약: 한 문장 + 완료 막대. */
@Composable
internal fun AssignmentSummaryCard(report: AssignmentReport) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(report.line, style = MaterialTheme.typography.titleSmall)
            if (report.total > 0) {
                LinearProgressIndicator(progress = { report.done.toFloat() / report.total }, modifier = Modifier.fillMaxWidth())
                Text(
                    "끝낸 과제 ${report.done}/${report.total} · 밀린 ${report.overdue.size} · 이번 주 마감 ${report.dueSoon.size}",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
