package com.nextstep.app.ui.components.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.goaltree.WeekRate
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.chart.ColumnChart

/** 주별 할 일 달성(그 주에 마감인 할 일 중 끝낸 몫, 최근 5주). 70% 선이 "잘 가고 있음"의 기준. */
@Composable
fun WeekRatesCard(weeks: List<WeekRate>, compact: Boolean) {
    AppCard {
        ColumnChart(
            values = weeks.map { it.percent },
            labels = weeks.map { DateUtils.formatShortDate(it.weekStart) },
            description = "주별 할 일 달성: " + weeks.joinToString { "${DateUtils.formatShortDate(it.weekStart)} 주 ${it.done}/${it.due}" },
            goal = GOOD_PERCENT,
            goalLabel = "${GOOD_PERCENT}%",
            max = FULL_PERCENT,
            format = { "$it%" },
            height = if (compact) COMPACT_DP.dp else FULL_DP.dp,
        )
    }
}

private const val GOOD_PERCENT = 70
private const val FULL_PERCENT = 100
private const val COMPACT_DP = 120
private const val FULL_DP = 150
