package com.nextstep.app.ui.mentor.components

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.stats.Submissions
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.ChartPalette
import com.nextstep.app.ui.components.chart.ShareBar
import com.nextstep.app.ui.components.chart.SharePart

/** 낸 과제의 상태 몫: 끝냄 · 기한 전 · 밀림(쌓은 막대 + 이름과 몫). */
@Composable
internal fun SubmissionsCard(s: Submissions) {
    val palette = ChartPalette.current()
    AppCard {
        ShareBar(
            listOf(
                SharePart("끝냄", s.done, palette.series[0]),
                SharePart("기한 전", s.pending, palette.series[2]),
                SharePart("밀림", s.late, palette.series[1]),
            ),
        )
    }
}
