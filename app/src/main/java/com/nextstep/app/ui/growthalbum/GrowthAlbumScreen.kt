package com.nextstep.app.ui.growthalbum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.plan.Feature
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.ExportShareRow
import com.nextstep.app.ui.components.input.SegmentedRow
import com.nextstep.app.ui.growthalbum.components.AlbumChapterBody
import com.nextstep.app.ui.growthalbum.components.AlbumChapterHeader
import com.nextstep.app.ui.growthalbum.components.AlbumChapterNav
import com.nextstep.app.ui.growthalbum.components.AlbumCover
import com.nextstep.app.ui.growthalbum.components.albumSummary
import com.nextstep.app.ui.theme.handStyle
import kotlinx.coroutines.launch

@Composable
fun GrowthAlbumScreen(caps: Capabilities, viewModel: GrowthAlbumViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GrowthAlbumContent(state, caps, viewModel::onEvent)
}

/** 올해의 성장 앨범: 올해|작년 → 표지(한 해 숫자) → 차례 → 장마다(해낸 것 · 꾸준함 · 해 본 것 · 자란 것 · 응원 · 이야기) → 보내기. 좋았던 것만 담습니다. */
@Composable
internal fun GrowthAlbumContent(state: GrowthAlbumUiState, caps: Capabilities, onEvent: (GrowthAlbumEvent) -> Unit) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val offsets = remember { mutableStateMapOf<AlbumChapter, Int>() }
    Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (!caps.has(Feature.GROWTH_ALBUM)) {
            EmptyCard("성장 앨범은 가족 플러스에서 열려요")
            return@Column
        }
        SegmentedRow(YEARS, YEARS[state.yearsBack], label = { it }, onSelect = { onEvent(GrowthAlbumEvent.SetYearsBack(YEARS.indexOf(it))) })
        val book = state.book ?: return@Column
        AlbumCover(book)
        if (book.isEmpty) {
            EmptyCard("이 학년도에는 아직 모인 순간이 없어요 · 목표를 이루거나 활동을 적으면 한 장씩 쌓여요")
            return@Column
        }
        val chapters = AlbumChapter.entries.filter { it.present(book) }
        AlbumChapterNav(chapters) { c -> scope.launch { offsets[c]?.let { scroll.animateScrollTo(it) } } }
        chapters.forEach { c ->
            Column(Modifier.onGloballyPositioned { offsets[c] = it.positionInParent().y.toInt() }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AlbumChapterHeader(c, albumSummary(c, book), Modifier.padding(top = 8.dp))
                AlbumChapterBody(c, book)
            }
        }
        Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("한 해를 한 권으로", style = handStyle(32.sp), color = MaterialTheme.colorScheme.onSurface)
            Text("할머니·할아버지께 보내 보세요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        state.album?.let { ExportShareRow(it, "nextstep-growth-album", pdf = caps.has(Feature.PDF_EXPORT)) }
    }
}

private val YEARS = listOf("올해", "작년")
