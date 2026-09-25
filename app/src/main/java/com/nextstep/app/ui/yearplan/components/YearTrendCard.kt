package com.nextstep.app.ui.yearplan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.year.YearTrend
import com.nextstep.app.ui.components.card.AppCard

/** 이 나이의 교육열 한 장: "주변에서는"(조사 숫자)과 "우리는"(권장 기준). 비교가 아니라 기준을 줍니다. */
@Composable
internal fun YearTrendCard(trend: YearTrend) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("주변에서는", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(trend.common, style = MaterialTheme.typography.bodyMedium)
            Text("우리는", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(trend.advice, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
