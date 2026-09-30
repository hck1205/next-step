package com.nextstep.app.ui.insights.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.nextstep.app.domain.stats.SubjectMinutes
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.DonutChart
import com.nextstep.app.ui.components.chart.Slice
import com.nextstep.app.ui.theme.subjectColor

/** 이번 주 과목별 시간 도넛. 과목 없이 한 공부는 "기타". */
@Composable
internal fun SubjectShareCard(weekly: List<SubjectMinutes>) {
    AppCard {
        DonutChart(
            slices = weekly.map { w -> Slice(w.subject?.name ?: "기타", w.minutes.toFloat(), w.subject?.let { subjectColor(it.color) } ?: MaterialTheme.colorScheme.onSurfaceVariant) },
            centerText = DateUtils.formatMinutes(weekly.sumOf { it.minutes }),
        )
    }
}
