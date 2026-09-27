package com.nextstep.app.ui.components.card

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.stats.Submissions
import com.nextstep.app.ui.components.chart.ChartPalette
import com.nextstep.app.ui.components.chart.ShareBar
import com.nextstep.app.ui.components.chart.SharePart

/** 낸 과제의 상태 몫: 끝냄 · 기한 전 · 밀림(쌓은 막대 + 이름과 몫). 멘토 오늘 화면의 카드. */
@Composable
fun SubmissionsCard(s: Submissions) {
    AppCard { SubmissionsBar(s) }
}

/** 카드 틀 없이 막대만(과제 섹션의 요약 카드 안). 색은 상태를 따라갑니다. */
@Composable
fun SubmissionsBar(s: Submissions) {
    val palette = ChartPalette.current()
    ShareBar(
        listOf(
            SharePart("끝냄", s.done, palette.series[0]),
            SharePart("기한 전", s.pending, palette.series[2]),
            SharePart("밀림", s.late, palette.series[1]),
        ),
    )
}
