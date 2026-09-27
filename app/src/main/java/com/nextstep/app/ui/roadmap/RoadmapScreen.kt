package com.nextstep.app.ui.roadmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.common.ExternalLinks
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.layout.BackButton
import com.nextstep.app.ui.components.layout.hostedSectionAdd
import com.nextstep.app.ui.roadmap.components.DoneToggleRow
import com.nextstep.app.ui.roadmap.components.RoadmapEditDialog
import com.nextstep.app.ui.roadmap.components.RoadmapRow
import com.nextstep.app.ui.roadmap.components.RoadmapSummaryCard
import com.nextstep.app.ui.roadmap.components.SuggestionChips

/**
 * 학습 로드맵. 멘토(또는 학부모 겸 멘토)가 큐레이팅하고, 학생이 진행 상태를 갱신하고, 학부모는 진행률을 봅니다.
 */
@Composable
fun RoadmapScreen(caps: Capabilities, actions: RoadmapActions, viewModel: RoadmapViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RoadmapContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoadmapContent(state: RoadmapUiState, caps: Capabilities, actions: RoadmapActions, onEvent: (RoadmapEvent) -> Unit) {
    val context = LocalContext.current
    var showEdit by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RoadmapItemEntity?>(null) }
    var showDone by remember { mutableStateOf(false) }
    // 기록 탭 안에서는 "만들기"가 상단 바로 올라가고, 따로 열었을 때만 + 버튼을 그립니다.
    val hosted = hostedSectionAdd(if (caps.canEditRoadmap) "항목 추가" else null) { editing = null; showEdit = true }
    val row: @Composable (RoadmapItemEntity) -> Unit = { item ->
        RoadmapRow(item, state.subjects, caps, linked = state.contentOf(item),
            onStatus = { onEvent(RoadmapEvent.SetStatus(item.id, it)) },
            onEdit = { editing = item; showEdit = true },
            onOpenLinked = { c -> ExternalLinks.open(context, c.url) })
    }

    Scaffold(
        // onBack 이 없으면 기록 탭의 섹션으로 들어간 것: 관심사·섹션 줄이 제목을 대신합니다.
        topBar = {
            if (actions.onBack != null) TopAppBar(
                title = { Text(if (caps.isStudent) "내 학습 로드맵" else "${state.studentName.ifBlank { "학생" }} 학습 로드맵") },
                navigationIcon = { BackButton(actions.onBack) },
                actions = { TextButton(onClick = actions.onOpenContent) { Text("콘텐츠") } },
            )
        },
        floatingActionButton = {
            if (!hosted && caps.canEditRoadmap) FloatingActionButton(onClick = { editing = null; showEdit = true }) { Icon(Icons.Default.Add, contentDescription = "로드맵 항목 추가") }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { RoadmapSummaryCard(state.completion, state.done.size, state.items.size, caps) }

            if (caps.canEditRoadmap && state.suggestions.isNotEmpty()) {
                item { SectionTitle("진도 기반 추천 (눌러서 추가)") }
                item { SuggestionChips(state.suggestions, onAdd = { subject, title -> onEvent(RoadmapEvent.AddSuggestion(subject, title)) }) }
            }

            item { SectionTitle("진행 중 · 예정") }
            if (state.active.isEmpty()) item { EmptyCard(if (caps.canEditRoadmap) "첫 로드맵 항목을 추가해 보세요" else "아직 제안된 로드맵이 없어요") }
            items(state.active, key = { it.id }) { row(it) }

            if (state.done.isNotEmpty()) {
                item { DoneToggleRow(state.done.size, showDone, onToggle = { showDone = it }) }
                if (showDone) items(state.done, key = { it.id }) { row(it) }
            }
        }
    }

    if (showEdit) {
        RoadmapEditDialog(editing, state.subjects, state.contents, onDismiss = { showEdit = false }, onDelete = editing?.let { e -> { onEvent(RoadmapEvent.Delete(e.id)) } }) { subjectId, title, desc, res, date, contentId ->
            onEvent(RoadmapEvent.Save(editing, subjectId, title, desc, res, date, contentId))
        }
    }
}
