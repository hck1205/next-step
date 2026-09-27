package com.nextstep.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.domain.today.ParentTodayCard
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.StatusCard
import com.nextstep.app.ui.components.card.StatusTile
import com.nextstep.app.ui.components.card.SyncStatusBadge
import com.nextstep.app.ui.components.input.ChildPicker
import com.nextstep.app.ui.components.layout.AppBarMenu
import com.nextstep.app.ui.components.layout.AppBarMenuItem
import com.nextstep.app.ui.components.layout.DetailSheet
import com.nextstep.app.ui.components.layout.ScreenPadding
import com.nextstep.app.ui.components.layout.todayBoard
import com.nextstep.app.ui.parent.components.ParentCardBody
import com.nextstep.app.ui.parent.components.parentCardTitle

/**
 * 학부모의 "오늘": 지금 뭐 하면 되지? 에만 답합니다.
 * 상태 문장 → 지금 챙길 것 → 오늘의 아이. 그래프·성적·진도는 기록 탭에 있습니다.
 */
@Composable
fun ParentDashboardScreen(caps: Capabilities, actions: ParentDashboardActions, viewModel: ParentDashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ParentDashboardContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParentDashboardContent(state: ParentDashboardUiState, caps: Capabilities, actions: ParentDashboardActions, onEvent: (ParentDashboardEvent) -> Unit) {
    var filter by rememberSaveable { mutableStateOf<Concern?>(null) }
    var sheet by remember { mutableStateOf<ParentTodayCard?>(null) }
    sheet?.let { card ->
        DetailSheet(parentCardTitle(card, state).ifBlank { card.title }, onDismiss = { sheet = null }) { ParentCardBody(card, state, actions, onEvent, compact = false) }
    }
    Scaffold(topBar = { ParentTopBar(state, caps, actions) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenPadding.list,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 상단 바 아래 관심사 칩 → 상태 요약 → "전체"는 관심사마다 카드 슬라이드, 칩을 고르면 그 관심사만 크게. 펼치기는 자세히 시트로.
            todayBoard(
                groups = state.todayGroups, filter = filter, onFilter = { filter = it },
                title = { parentCardTitle(it, state) }, key = { it.name }, onExpand = { sheet = it },
                lead = { ParentStatus(state) },
                body = { card, compact -> ParentCardBody(card, state, actions, onEvent, compact) },
            )
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/** 머리: "오늘" · 날짜·동기화, 오른쪽에 자녀 고르기와 ⋮(영상 저장소 · 멘토 화면). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentTopBar(state: ParentDashboardUiState, caps: Capabilities, actions: ParentDashboardActions) {
    TopAppBar(
        title = {
            Column {
                Text("오늘", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(DateUtils.formatFullDate(state.today), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SyncStatusBadge(state.syncStatus)
                }
            }
        },
        actions = {
            ChildPicker(state.children, state.activeFamilyId, onSelect = actions.onSwitchChild, onAdd = actions.onOpenSettings)
            AppBarMenu(
                listOfNotNull(
                    AppBarMenuItem("영상 저장소", Icons.Default.SmartDisplay, actions.onOpenContent),
                    if (caps.actsAsMentor) AppBarMenuItem("멘토 화면", Icons.Default.School, actions.onOpenMentor) else null,
                ),
            )
        },
    )
}

/** 이번 주 상태 요약(학습 시간 · 균형 · 스스로). 관심사 칩 아래, "전체"일 때만. */
@Composable
private fun ParentStatus(state: ParentDashboardUiState) {
    val b = state.balance
    StatusCard(
        context = "${state.studentName.ifBlank { "자녀" }} · ${state.statusContext}",
        headline = state.statusHeadline,
        tiles = listOfNotNull(
            StatusTile("이번 주 학습", DateUtils.formatMinutes(state.weekMinutes)),
            b?.let { StatusTile("균형", it.studyVerdict.label) },
            b?.selfDirectedRatio?.let { StatusTile("스스로", "${(it * PERCENT).toInt()}%") } ?: StatusTile("연속", "${state.streak}일"),
        ),
    )
}

private const val PERCENT = 100
