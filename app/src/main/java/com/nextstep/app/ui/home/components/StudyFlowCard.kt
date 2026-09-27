package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.DayMinutes
import com.nextstep.app.domain.stats.HeatWeek
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.ColumnChart
import com.nextstep.app.ui.components.chart.HeatCalendar
import java.time.LocalDate

/**
 * 나의 공부 흐름: 최근 5주 공부 달력(칸이 채워지는 재미), 자세히 보면 최근 7일 막대와 올해 하루 권장량 선.
 * 내 기록만 보여 주고 남과 견주지 않습니다.
 */
@Composable
internal fun StudyFlowCard(heat: List<HeatWeek>, week: List<DayMinutes>, dailyGoal: Int, today: LocalDate, compact: Boolean) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            HeatCalendar(heat, today, compact = compact)
            if (!compact && week.isNotEmpty()) {
                ColumnChart(
                    values = week.map { it.minutes },
                    labels = week.map { DateUtils.dayOfWeekLabel(it.date.dayOfWeek) },
                    description = "최근 7일 공부: " + week.joinToString { "${DateUtils.dayOfWeekLabel(it.date.dayOfWeek)} ${it.minutes}분" },
                    goal = dailyGoal.takeIf { it > 0 },
                    goalLabel = "올해 하루 권장 ${dailyGoal}분",
                    format = { "${it}분" },
                )
            }
        }
    }
}
