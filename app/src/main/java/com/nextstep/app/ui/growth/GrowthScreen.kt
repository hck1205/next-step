package com.nextstep.app.ui.growth

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.LineChart
import com.nextstep.app.ui.components.chart.LineSeries
import com.nextstep.app.ui.growth.components.GrowthCard
import com.nextstep.app.ui.growth.components.GrowthRecordDialog
import com.nextstep.app.ui.growth.components.GrowthRecordRow

/** 성장 › 신체. 요약 → 키 추이 → 연도별 전체 기록(해 머리는 위에 붙어 따라옵니다). */
@Composable
fun GrowthScreen(caps: Capabilities, viewModel: GrowthViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GrowthContent(state = state, caps = caps, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun GrowthContent(state: GrowthUiState, caps: Capabilities, onEvent: (GrowthEvent) -> Unit) {
    var editing by remember { mutableStateOf<GrowthRecordEntity?>(null) }
    var showEdit by remember { mutableStateOf(false) }
    if (showEdit) GrowthRecordDialog(existing = editing, today = state.today, onConfirm = { onEvent(GrowthEvent.Save(it)); showEdit = false }, onDismiss = { showEdit = false })

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { GrowthCard(summary = state.summary, onAdd = if (caps.canRecordGrowth) ({ editing = null; showEdit = true }) else null) }
        if (state.heightTrend.size >= MIN_TREND) item {
            AppCard {
                val heights = state.heightTrend.map { it.second.toFloat() }
                LineChart(
                    series = listOf(LineSeries("키", MaterialTheme.colorScheme.primary, heights)),
                    xLabels = state.heightTrend.map { it.first },
                    yMin = heights.min() - CHART_MARGIN_CM, yMax = heights.max() + CHART_MARGIN_CM, height = CHART_HEIGHT,
                )
            }
        }
        state.byYear.forEach { (year, records) ->
            stickyHeader(key = "y$year") {
                Text(
                    "${year}년 · ${records.size}번", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 6.dp),
                )
            }
            items(records, key = { it.id }) { r ->
                GrowthRecordRow(r, onEdit = { editing = r; showEdit = true }, onDelete = if (caps.canRecordGrowth) ({ onEvent(GrowthEvent.Delete(r.id)) }) else null)
            }
        }
    }
}

private const val MIN_TREND = 2
private const val CHART_MARGIN_CM = 2f
private const val CHART_HEIGHT = 140
