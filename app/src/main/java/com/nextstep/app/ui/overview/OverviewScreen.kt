package com.nextstep.app.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.overview.components.BalanceCard
import com.nextstep.app.ui.overview.components.ConcernTile

/**
 * 기록 › 한눈에. 균형 카드 한 장 + 관심사 타일 2열 격자.
 * [concerns] 는 지금 보이는 관심사(학생 화면 단계에 따라 줄어듦)이며, 그 타일만 그립니다.
 */
@Composable
fun OverviewScreen(concerns: List<Concern>, actions: OverviewActions, viewModel: OverviewViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OverviewContent(state = state, concerns = concerns, actions = actions)
}

@Composable
internal fun OverviewContent(state: OverviewUiState, concerns: List<Concern>, actions: OverviewActions) {
    val tiles = concerns.mapNotNull { c -> state.digests.firstOrNull { it.concern == c } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            val b = state.balance
            if (b != null) BalanceCard(b, state.yearLabel ?: state.stage?.label) else AppCard { EmptyState("기록이 쌓이면 균형을 보여 드려요") }
        }
        if (tiles.isNotEmpty()) item { SectionTitle("관심사별") }
        items(tiles.chunked(TILE_COLUMNS), key = { row -> row.first().concern.name }) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { d -> ConcernTile(d, onClick = { actions.onOpenConcern(d.concern) }, modifier = Modifier.weight(1f)) }
                if (row.size < TILE_COLUMNS) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private const val TILE_COLUMNS = 2
