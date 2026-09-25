package com.nextstep.app.ui.habits.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.BarChart
import com.nextstep.app.ui.components.chart.BarItem
import java.time.DayOfWeek

/** 요일별 공부 시간(4주 합계, 분). */
@Composable
internal fun WeekdayCard(byWeekday: Map<DayOfWeek, Int>) {
    val color = MaterialTheme.colorScheme.secondary
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("요일별 (4주 합계)", style = MaterialTheme.typography.titleSmall)
            BarChart(
                items = DayOfWeek.entries.map { BarItem(DateUtils.dayOfWeekLabel(it), (byWeekday[it] ?: 0).toFloat(), color) },
                valueFormatter = { if (it <= 0f) "" else DateUtils.formatMinutes(it.toInt()) },
                height = 120,
            )
        }
    }
}
