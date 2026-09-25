package com.nextstep.app.ui.yearplan

import com.nextstep.app.domain.access.Capabilities
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.speech.rememberSpeaker
import com.nextstep.app.ui.yearplan.components.YearTaskDialog
import com.nextstep.app.ui.yearplan.components.YearTaskRow
import com.nextstep.app.ui.yearplan.components.YearTrendCard
import com.nextstep.app.domain.year.YearDoer

/**
 * "올해" 화면(학생은 하단 탭, 학부모·멘토는 여정에서): 올해(만 나이·학년) 해야 할 일을 분류 탭(전체 · 국어 · 수학 · 영어 · … · 생활)으로 잘게 나눠 보여 줍니다.
 * 탭 안은 지금 학기 → 1년 내내 → 다른 학기 순서이고, 각 줄은 체크 한 번으로 끝납니다. 학년은 여기서 고르지 않습니다(생년월일로 자동).
 */
@Composable
fun YearPlanScreen(caps: Capabilities, actions: YearPlanActions, viewModel: YearPlanViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(caps.yearDoers) { viewModel.onEvent(YearPlanEvent.SetMine(caps.yearDoers)) }
    YearPlanContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun YearPlanContent(state: YearPlanUiState, actions: YearPlanActions, onEvent: (YearPlanEvent) -> Unit) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var open by remember { mutableStateOf<YearTaskView?>(null) }
    val speak = if (state.level.kid.readsAloud) rememberSpeaker() else null
    val numbers = state.level.showsNumbers
    open?.let { v -> YearTaskDialog(v, onToggle = { onEvent(YearPlanEvent.Toggle(v)) }, onAddToToday = { onEvent(YearPlanEvent.AddToToday(v.task)) }, onSpeak = speak, onDismiss = { open = null }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.year?.let { "${it.label} · 올해 할 일" } ?: "올해 할 일") },
                navigationIcon = { actions.onBack?.let { back -> IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } } },
                actions = { actions.onOpenJourney?.let { TextButton(onClick = it) { Text("여정") } } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            val year = state.year
            if (year == null || state.tabs.isEmpty()) {
                AppCard(Modifier.padding(16.dp)) { EmptyState("가족 탭에서 생년월일을 넣으면 올해 할 일이 채워져요") }
            } else {
                AppCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(year.theme, style = MaterialTheme.typography.titleMedium)
                        LinearProgressIndicator(progress = { if (state.total == 0) 0f else state.done.toFloat() / state.total }, modifier = Modifier.fillMaxWidth())
                        Text(
                            if (numbers) "${state.done} / ${state.total} 끝냈어요 · 지금 ${state.currentTerm.label}" else "별 ${state.done}개 모았어요",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (state.mineCount > 0 && state.mineCount < state.allCount) {
                    Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = state.mineOnly, onClick = { onEvent(YearPlanEvent.ShowMine(true)) }, label = { Text("내 할 일 ${state.mineCount}") })
                        FilterChip(selected = !state.mineOnly, onClick = { onEvent(YearPlanEvent.ShowMine(false)) }, label = { Text("가족 전체 ${state.allCount}") })
                    }
                }
                val selected = tabIndex.coerceIn(0, state.tabs.lastIndex)
                ScrollableTabRow(selectedTabIndex = selected, edgePadding = 12.dp) {
                    state.tabs.forEachIndexed { i, tab ->
                        Tab(selected = i == selected, onClick = { tabIndex = i }, text = { Text(if (numbers) "${tab.label} ${tab.done}/${tab.total}" else tab.label) })
                    }
                }
                val tab = state.tabs[selected]
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.trend?.let { t -> if (tab.area == null) item(key = "trend") { YearTrendCard(t) } }
                    tab.sections.forEach { (term, views) ->
                        stickyHeader(key = "t${tab.label}${term.name}") {
                            Text(
                                "${term.label} · ${term.months}" + if (term == state.currentTerm) " · 지금" else "",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (term == state.currentTerm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 6.dp),
                            )
                        }
                        items(views, key = { "${tab.label}${it.task.key}" }) { v ->
                            YearTaskRow(v, minHeightDp = state.level.touchTargetDp, showArea = tab.area == null, showDoer = state.showsAllDoers || v.task.who != YearDoer.CHILD, onToggle = { onEvent(YearPlanEvent.Toggle(v)) }, onOpen = { open = v })
                        }
                    }
                }
            }
        }
    }
}
