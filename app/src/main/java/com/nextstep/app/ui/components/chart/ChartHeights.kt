package com.nextstep.app.ui.components.chart

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 차트 높이 한 곳. 슬라이드(줄인 카드)에서는 낮게, 자세히·섹션 화면에서는 넉넉히. */
object ChartHeights {
    /** 세로 막대(ColumnChart). */
    fun column(compact: Boolean): Dp = if (compact) 120.dp else 150.dp

    /** 과목별 점수 작은 선(ScoreMultiples) 한 칸. */
    fun scoreLine(compact: Boolean): Dp = if (compact) 26.dp else 40.dp
}
