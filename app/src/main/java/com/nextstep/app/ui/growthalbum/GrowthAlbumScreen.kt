package com.nextstep.app.ui.growthalbum

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
import com.nextstep.app.domain.plan.Feature
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.ExportDocCard
import com.nextstep.app.ui.components.card.ExportShareRow
import com.nextstep.app.ui.components.input.SegmentedRow

@Composable
fun GrowthAlbumScreen(caps: Capabilities, viewModel: GrowthAlbumViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GrowthAlbumContent(state, caps, viewModel::onEvent)
}

/** 올해의 성장 앨범: 올해|작년 → 앨범 한 권 → 할머니·할아버지께 보내기(글 · PDF). 좋았던 것만 담습니다. */
@Composable
internal fun GrowthAlbumContent(state: GrowthAlbumUiState, caps: Capabilities, onEvent: (GrowthAlbumEvent) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!caps.has(Feature.GROWTH_ALBUM)) {
            EmptyCard("성장 앨범은 가족 플러스에서 열려요")
            return@Column
        }
        SegmentedRow(YEARS, YEARS[state.yearsBack], label = { it }, onSelect = { onEvent(GrowthAlbumEvent.SetYearsBack(YEARS.indexOf(it))) })
        Text("한 해 동안 쌓인 좋았던 순간을 한 권으로 모았어요 📔", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        state.album?.let { doc ->
            ExportDocCard(doc)
            ExportShareRow(doc, "nextstep-growth-album", pdf = caps.has(Feature.PDF_EXPORT))
        }
    }
}

private val YEARS = listOf("올해", "작년")
