package com.nextstep.app.ui.progress.components

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.BulletBars
import com.nextstep.app.ui.components.chart.BulletRow
import com.nextstep.app.ui.theme.subjectColor

/**
 * 수업한 단원 대비 복습: 막대 = 복습까지 한 단원, 세로 선 = 수업에서 나간 단원. 막대가 선에 못 미치는 과목이 복습이 밀린 과목입니다
 * (멘토·학부모가 어디부터 챙길지 한눈에).
 */
@Composable
internal fun ReviewGapCard(progress: List<SubjectProgress>) {
    AppCard {
        BulletBars(
            progress.map { p -> BulletRow(p.subject.name, p.reviewed, p.classCovered, "${p.reviewed}단원", "${p.classCovered}단원", subjectColor(p.subject.color)) },
            doneLabel = "복습", goalLabel = "수업한 단원",
        )
    }
}
