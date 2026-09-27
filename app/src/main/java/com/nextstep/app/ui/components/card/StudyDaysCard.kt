package com.nextstep.app.ui.components.card

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.stats.DayMinutes
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.chart.ChartHeights
import com.nextstep.app.ui.components.chart.ColumnChart

/** 요일별 공부 시간(최근 7일, 오늘이 맨 끝). 하루 목표가 있으면 목표선. */
@Composable
fun StudyDaysCard(days: List<DayMinutes>, dailyGoal: Int, compact: Boolean) {
    AppCard {
        ColumnChart(
            values = days.map { it.minutes },
            labels = days.map { DateUtils.dayOfWeekLabel(it.date.dayOfWeek) },
            description = "최근 7일 공부: " + days.joinToString { "${DateUtils.dayOfWeekLabel(it.date.dayOfWeek)} ${it.minutes}분" },
            goal = dailyGoal.takeIf { it > 0 },
            goalLabel = "하루 목표 ${dailyGoal}분",
            format = { "${it}분" },
            height = ChartHeights.column(compact),
        )
    }
}

