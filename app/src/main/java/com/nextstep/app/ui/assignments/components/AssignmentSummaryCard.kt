package com.nextstep.app.ui.assignments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.mentor.AssignmentReport
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubmissionsBar

/** 과제 요약: 한 문장 + 끝냄·기한 전·밀림의 몫(쌓은 막대, 이름과 몫은 범례에 글로). */
@Composable
internal fun AssignmentSummaryCard(report: AssignmentReport) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(report.line, style = MaterialTheme.typography.titleSmall)
            if (report.total > 0) SubmissionsBar(report.submissions)
        }
    }
}
