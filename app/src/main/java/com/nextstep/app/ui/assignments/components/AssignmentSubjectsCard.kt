package com.nextstep.app.ui.assignments.components

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.mentor.AssignmentSubject
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.subjectColor
import com.nextstep.app.ui.components.chart.BulletBars
import com.nextstep.app.ui.components.chart.BulletRow

/** 과목별 과제: 막대 = 끝낸 과제, 세로 선 = 낸 과제(낮은 과목 먼저). 줄 앞 작은 표시는 과목 색. */
@Composable
internal fun AssignmentSubjectsCard(bySubject: List<AssignmentSubject>) {
    AppCard {
        BulletBars(
            bySubject.map { s -> BulletRow(s.subject?.name ?: "과목 없음", s.done, s.total, "${s.done}개", "${s.total}개", s.subject?.let { subjectColor(it.color) }) },
            doneLabel = "끝냄", goalLabel = "낸 과제",
        )
    }
}
