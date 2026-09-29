package com.nextstep.app.ui.periodreport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.period.MonthPoint
import com.nextstep.app.domain.period.PeriodKind
import com.nextstep.app.domain.plan.Feature
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.ExportDocCard
import com.nextstep.app.ui.components.card.ExportShareRow
import com.nextstep.app.ui.components.chart.ColumnChart
import com.nextstep.app.ui.components.input.SegmentedRow

@Composable
fun PeriodReportScreen(caps: Capabilities, viewModel: PeriodReportViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PeriodReportContent(state, caps, viewModel::onEvent)
}

/** 월간·학기 리포트: 월간|학기 → 리포트(앞 기간과 견줌) → 보내기 → 학년도 긴 흐름(달마다 공부 시간 · 할 일 끝낸 비율). */
@Composable
internal fun PeriodReportContent(state: PeriodReportUiState, caps: Capabilities, onEvent: (PeriodReportEvent) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!caps.has(Feature.PERIOD_REPORT)) {
            EmptyCard("월간·학기 리포트는 가족 플러스에서 열려요")
            return@Column
        }
        SegmentedRow(PeriodKind.entries, state.kind, label = { it.label }, onSelect = { onEvent(PeriodReportEvent.SetKind(it)) })
        state.report?.let { doc ->
            ExportDocCard(doc)
            ExportShareRow(doc, "nextstep-${state.kind.name.lowercase()}-report", pdf = caps.has(Feature.PDF_EXPORT))
        }
        if (caps.has(Feature.LONG_TRENDS) && state.months.isNotEmpty()) LongTrends(state.months)
    }
}

@Composable
private fun LongTrends(months: List<MonthPoint>) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("올해 달마다 공부 시간", style = MaterialTheme.typography.titleSmall)
            ColumnChart(months.map { it.minutes }, months.map { it.label }, "달마다 공부 시간", format = { DateUtils.formatMinutes(it) }, tickFormat = { "${it / MINUTES_IN_HOUR}h" })
            Text("달마다 할 일 끝낸 비율", style = MaterialTheme.typography.titleSmall)
            ColumnChart(months.map { it.doneRate ?: 0 }, months.map { it.label }, "달마다 할 일 끝낸 비율", max = PERCENT, format = { "$it%" })
        }
    }
}

private const val MINUTES_IN_HOUR = 60
private const val PERCENT = 100
