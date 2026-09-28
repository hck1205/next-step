package com.nextstep.app.ui.yearplan

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.year.YearTerm
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.layout.BackButton
import com.nextstep.app.ui.components.layout.CompactTopBar
import com.nextstep.app.ui.components.layout.ScreenPadding
import com.nextstep.app.ui.components.speech.rememberSpeaker
import com.nextstep.app.ui.yearplan.components.AheadHeader
import com.nextstep.app.ui.yearplan.components.MineFilterRow
import com.nextstep.app.ui.yearplan.components.TermHeader
import com.nextstep.app.ui.yearplan.components.YearSummaryCard
import com.nextstep.app.ui.yearplan.components.YearTaskDialog
import com.nextstep.app.ui.yearplan.components.YearTaskGroup
import com.nextstep.app.ui.yearplan.components.YearTrendCard

/**
 * "올해" 화면(학생은 하단 탭, 학부모·멘토는 여정에서): 올해(만 나이·학년) 해야 할 일을 분류 탭(전체 · 국어 · 수학 · 영어 · … · 생활)으로 잘게 나눠 보여 줍니다.
 * 탭 안은 기본(지금 학기 → 1년 내내 → 다른 학기, 안 한 것이 먼저, 줄마다 "이만큼이면 충분" 기준)과 그 아래 앞서 가기(여유가 있을 때만)로 나뉘고,
 * 각 줄은 체크 한 번으로 끝납니다. 진행 막대는 기본만 셉니다. 학년은 여기서 고르지 않습니다(생년월일로 자동).
 */
@Composable
fun YearPlanScreen(caps: Capabilities, actions: YearPlanActions, viewModel: YearPlanViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(caps.yearDoers) { viewModel.onEvent(YearPlanEvent.SetMine(caps.yearDoers)) }
    YearPlanContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun YearPlanContent(state: YearPlanUiState, actions: YearPlanActions, onEvent: (YearPlanEvent) -> Unit) {
    var open by remember { mutableStateOf<YearTaskView?>(null) }
    val speak = if (state.level.kid.readsAloud) rememberSpeaker() else null
    open?.let { v -> YearTaskDialog(v, onToggle = { onEvent(YearPlanEvent.Toggle(v)) }, onAddToToday = { onEvent(YearPlanEvent.AddToToday(v.task)) }, onSpeak = speak, onDismiss = { open = null }) }

    Scaffold(
        topBar = {
            CompactTopBar(
                title = "올해 할 일", caption = state.year?.label,
                navigationIcon = { BackButton(actions.onBack) },
                actions = { actions.onOpenJourney?.let { TextButton(onClick = it) { Text("여정") } } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            val year = state.year
            if (year == null || state.tabs.isEmpty()) {
                AppCard(Modifier.padding(16.dp)) { EmptyState("가족 탭에서 생년월일을 넣으면 올해 할 일이 채워져요") }
            } else {
                YearBody(state, year.theme, onEvent, onOpen = { open = it })
            }
        }
    }
}

/** 요약 · 내 할 일 칩 · 분류 탭 · 탭 목록. */
@Composable
private fun YearBody(state: YearPlanUiState, theme: String, onEvent: (YearPlanEvent) -> Unit, onOpen: (YearTaskView) -> Unit) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var openTerms by rememberSaveable { mutableStateOf(setOf<String>()) }
    // 분류 탭은 이 화면의 메뉴라 상단 바 바로 아래, 요약·내 할 일 칩은 목록의 맨 위(내용과 함께 스크롤).
    val selected = tabIndex.coerceIn(0, state.tabs.lastIndex)
    ScrollableTabRow(selectedTabIndex = selected, edgePadding = 12.dp) {
        state.tabs.forEachIndexed { i, tab ->
            Tab(selected = i == selected, onClick = { tabIndex = i }, text = { Text(if (state.level.showsNumbers) "${tab.label} ${tab.done}/${tab.total}" else tab.label) })
        }
    }
    val lists = TabLists(
        state, onToggle = { onEvent(YearPlanEvent.Toggle(it)) }, onOpen = onOpen,
        openTerms = openTerms, onFold = { key -> openTerms = if (key in openTerms) openTerms - key else openTerms + key },
    )
    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding.list, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item(key = "summary") { YearSummaryCard(state, theme) }
        if (state.mineCount > 0 && state.mineCount < state.allCount) item(key = "mine") {
            MineFilterRow(state.mineOnly, state.mineCount, state.allCount, onShowMine = { onEvent(YearPlanEvent.ShowMine(it)) })
        }
        yearTab(state.tabs[selected], lists)
    }
}

/** 탭 목록이 쓰는 상태와 콜백 묶음. */
private class TabLists(
    val state: YearPlanUiState,
    val onToggle: (YearTaskView) -> Unit,
    val onOpen: (YearTaskView) -> Unit,
    val openTerms: Set<String>,
    val onFold: (String) -> Unit,
)

/** 한 탭의 목록: (전체 탭이면) 추세 → 학기별 기본(다른 학기는 접힘) → 앞서 가기. */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.yearTab(tab: YearTab, lists: TabLists) {
    val state = lists.state
    state.trend?.let { t -> if (tab.area == null) item(key = "trend") { YearTrendCard(t) } }
    tab.sections.forEach { (term, views) ->
        // 지금 학기와 1년 내내는 펼쳐 두고, 다른 학기는 접어 둡니다(누르면 펼침).
        val termKey = "${tab.label}:${term.name}"
        val folds = term != state.currentTerm && term != YearTerm.ALL_YEAR
        val expanded = !folds || termKey in lists.openTerms
        stickyHeader(key = "t$termKey") {
            TermHeader(term, isCurrent = term == state.currentTerm, count = views.size, expanded = expanded, onFold = if (folds) ({ lists.onFold(termKey) }) else null)
        }
        if (expanded) item(key = "g$termKey") { TaskGroup(views, termKey, tab, lists) }
    }
    if (tab.ahead.isNotEmpty()) {
        item(key = "ahead-head-${tab.label}") { AheadHeader(state.aheadHeading, state.aheadNote) }
        item(key = "ahead-${tab.label}") { TaskGroup(tab.ahead, "ahead:${tab.label}", tab, lists) }
    }
}

@Composable
private fun TaskGroup(views: List<YearTaskView>, key: String, tab: YearTab, lists: TabLists) {
    YearTaskGroup(
        views, key = key, minHeightDp = lists.state.level.touchTargetDp, showArea = tab.area == null, showsAllDoers = lists.state.showsAllDoers,
        onToggle = lists.onToggle, onOpen = lists.onOpen,
    )
}
