package com.nextstep.app.ui.insights.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.nextstep.app.domain.stats.DayMinutes
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.BarChart
import com.nextstep.app.ui.components.chart.BarItem

/** 날마다 공부한 시간 막대. 날짜는 하루 걸러 적어 겹치지 않게 합니다. */
@Composable
internal fun StudyDaysCard(days: List<DayMinutes>) {
    AppCard {
        BarChart(
            items = days.map { d -> BarItem(if (d.date.dayOfMonth % 2 == 1) d.date.dayOfMonth.toString() else "", d.minutes.toFloat(), MaterialTheme.colorScheme.primary) },
            valueFormatter = { if (it >= DateUtils.MINUTES_IN_HOUR) "${(it / DateUtils.MINUTES_IN_HOUR).toInt()}h" else "${it.toInt()}m" },
            height = CHART_HEIGHT,
        )
    }
}

private const val CHART_HEIGHT = 140
