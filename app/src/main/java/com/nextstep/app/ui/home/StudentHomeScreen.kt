package com.nextstep.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.growth.StudentHomeSection
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.layout.DetailSheet
import com.nextstep.app.ui.components.layout.TodayCardFrame
import com.nextstep.app.ui.components.layout.todayBoard
import com.nextstep.app.ui.components.speech.rememberSpeaker
import com.nextstep.app.ui.home.components.HomeSectionBody
import com.nextstep.app.ui.home.components.HomeTopBar
import com.nextstep.app.ui.home.components.LevelUpCard
import com.nextstep.app.ui.home.components.PlanResultDialog
import com.nextstep.app.ui.home.components.PlannerDialog
import com.nextstep.app.ui.home.components.homeSectionTitle

@Composable
fun StudentHomeScreen(actions: HomeActions, viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(state: HomeUiState, actions: HomeActions, onEvent: (HomeEvent) -> Unit) {
    var showPlanner by remember { mutableStateOf(false) }
    var filter by rememberSaveable { mutableStateOf<Concern?>(null) }
    var sheet by remember { mutableStateOf<StudentHomeSection?>(null) }
    val level = state.level
    val speak = if (level.kid.readsAloud) rememberSpeaker() else null
    Scaffold(topBar = { HomeTopBar(state) }) { padding ->
        // 카드는 화면 단계(level)가 연 것만 그립니다. 학년으로 직접 분기하지 않습니다.
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(if (level.showsNumbers) 10.dp else 14.dp),
        ) {
            state.levelUp?.let { up -> item { LevelUpCard(up, state.newSections, onOk = { onEvent(HomeEvent.DismissLevelUp) }) } }
            val body: @Composable (StudentHomeSection, Boolean) -> Unit = { section, compact ->
                HomeSectionBody(section, state, actions, onEvent, compact, onSpeak = speak, onOpenPlanner = { showPlanner = true })
            }
            // 타이머는 늘 맨 위. 나머지 카드는 올해 프로필 순서(StudentScreen.homeOrder)대로입니다. 학년으로 직접 분기하지 않습니다.
            if (StudentHomeSection.TIMER in state.visibleSections && filter == null) item(key = "timer") { body(StudentHomeSection.TIMER, false) }
            if (level.kid.oneColumnToday) {
                // 아이 화면: 관심사 칩·슬라이드 없이 큰 카드 한 줄로.
                state.visibleSections.filter { it != StudentHomeSection.TIMER }.forEach { section ->
                    item(key = "one-${section.name}") { TodayCardFrame(homeSectionTitle(section, state), onExpand = null) { body(section, false) } }
                }
            } else {
                // 관심사 칩 → "전체"는 관심사마다 카드 슬라이드, 칩을 고르면 그 관심사만 크게. 펼치기는 자세히 시트로.
                todayBoard(
                    groups = state.todayGroups, filter = filter, onFilter = { filter = it },
                    title = { homeSectionTitle(it, state) }, key = { it.name }, onExpand = { sheet = it },
                    body = body,
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    sheet?.let { section ->
        DetailSheet(homeSectionTitle(section, state).ifBlank { section.label }, onDismiss = { sheet = null }) {
            HomeSectionBody(section, state, actions, onEvent, compact = false, onSpeak = speak, onOpenPlanner = { sheet = null; showPlanner = true })
        }
    }
    if (showPlanner) {
        PlannerDialog(defaults = state.planDefaults, onDismiss = { showPlanner = false }) { onEvent(HomeEvent.GeneratePlan(it)) }
    }
    state.lastPlan?.let { plan -> PlanResultDialog(plan, onDismiss = { onEvent(HomeEvent.DismissPlanResult) }) }
}
