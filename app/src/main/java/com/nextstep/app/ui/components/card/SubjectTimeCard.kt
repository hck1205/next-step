package com.nextstep.app.ui.components.card

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.stats.SubjectMinutes
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.chart.BulletBars
import com.nextstep.app.ui.components.chart.BulletRow

/** 과목별 이번 주 공부 시간과 주 목표(목표 대비 막대). 줄 앞 작은 색 표시는 과목 색. */
@Composable
fun SubjectTimeCard(bySubject: List<SubjectMinutes>) {
    AppCard {
        BulletBars(
            bySubject.mapNotNull { m ->
                m.subject?.let { s ->
                    BulletRow(s.name, m.minutes, m.goalMinutes, DateUtils.formatMinutes(m.minutes), DateUtils.formatMinutes(m.goalMinutes), subjectColor(s.color))
                }
            },
            goalLabel = "주 목표",
        )
    }
}
