package com.nextstep.app.ui.components.card

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.stats.ScoreSeries
import com.nextstep.app.ui.components.chart.ChartHeights
import com.nextstep.app.ui.components.chart.ScoreMultiples

/** 과목별 점수 흐름 카드(학부모·멘토·학생 오늘 화면이 같이 씀). */
@Composable
fun ScoreTrendCard(series: List<ScoreSeries>, compact: Boolean) {
    AppCard { ScoreMultiples(series, lineHeight = ChartHeights.scoreLine(compact)) }
}
