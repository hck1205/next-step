package com.nextstep.app.ui.components.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.chart.ColumnChart
import java.time.LocalDate

/** 7일씩 8번의 공부 시간(마지막이 최근 7일). 막대 이름은 그 7일이 끝나는 날, 주 목표가 있으면 목표선. */
@Composable
fun StudyWeeksCard(rolling: List<Int>, weeklyGoal: Int, today: LocalDate, compact: Boolean) {
    val ends = rolling.indices.map { k -> today.minusDays(DAYS_IN_WEEK * (rolling.size - 1 - k)) }
    AppCard {
        ColumnChart(
            values = rolling,
            labels = ends.map(DateUtils::formatShortDate),
            description = "7일씩 공부 시간: " + rolling.joinToString { DateUtils.formatMinutes(it) },
            goal = weeklyGoal.takeIf { it > 0 },
            goalLabel = "주 목표 ${DateUtils.formatMinutes(weeklyGoal)}",
            format = DateUtils::formatMinutes,
            tickFormat = { "${it}분" },
            height = if (compact) COMPACT_DP.dp else FULL_DP.dp,
        )
    }
}

private const val DAYS_IN_WEEK = 7L
private const val COMPACT_DP = 120
private const val FULL_DP = 150
