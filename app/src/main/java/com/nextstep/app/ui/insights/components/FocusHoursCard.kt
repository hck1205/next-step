package com.nextstep.app.ui.insights.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.HourHeatStrip

/** 시간대별 집중 분포(24칸)와 누적 시간. */
@Composable
internal fun FocusHoursCard(byHour: IntArray, totalMinutes: Int) {
    AppCard {
        Column {
            HourHeatStrip(byHour, MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text("누적 ${DateUtils.formatMinutes(totalMinutes)} · 진한 칸일수록 그 시간대에 많이 공부했어요", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
