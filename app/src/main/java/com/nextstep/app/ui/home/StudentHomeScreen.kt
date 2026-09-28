package com.nextstep.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.growth.StudentHomeSection
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.layout.AppBarMenuItem
import com.nextstep.app.ui.components.layout.DetailSheet
import com.nextstep.app.ui.components.layout.ScreenPadding
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
    val body: @Composable (StudentHomeSection, Boolean) -> Unit = { section, compact ->
        HomeSectionBody(section, state, actions, onEvent, compact, onSpeak = speak, onOpenPlanner = { showPlanner = true })
    }
    // 단계가 오른 것 알림과 타이머는 묶음에 들지 않는 머리 카드: "전체"의 맨 위에만.
    val lead: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(if (level.showsNumbers) 10.dp else 14.dp)) {
            state.levelUp?.let { up -> LevelUpCard(up, state.newSections, onOk = { onEvent(HomeEvent.DismissLevelUp) }) }
            if (StudentHomeSection.TIMER in state.visibleSections) body(StudentHomeSection.TIMER, false)
        }
    }
    val scroll = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { HomeTopBar(state, menu = homeMenu(state, actions, onOpenPlanner = { showPlanner = true }), scrollBehavior = scroll) },
    ) { padding ->
        // 카드는 화면 단계(level)가 연 것만 그립니다. 학년으로 직접 분기하지 않습니다.
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenPadding.list,
            verticalArrangement = Arrangement.spacedBy(if (level.showsNumbers) 10.dp else 14.dp),
        ) {
            if (level.kid.oneColumnToday) {
                // 아이 화면: 접지 않고 큰 카드 한 줄로.
                item(key = "today-lead") { lead() }
                state.visibleSections.filter { it != StudentHomeSection.TIMER }.forEach { section ->
                    item(key = "one-${section.name}") { TodayCardFrame(homeSectionTitle(section, state), onExpand = null) { body(section, false) } }
                }
            } else {
                // 머리 카드 → "먼저 볼 것"(오늘 할 일) → "더 보기"(관심사마다 한 줄, 올해 프로필 순서). 펼치기는 자세히 시트로.
                todayBoard(
                    groups = state.todayGroups, filter = filter, onFilter = { filter = it },
                    title = { homeSectionTitle(it, state) }, key = { it.name }, onExpand = { sheet = it }, preferred = StudentHomeSection.FOCUS,
                    lead = lead, body = body,
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

/** 머리 ⋮ 메뉴: 영상 저장소(추천 영상이 열린 단계부터)와, 카드 대신 메뉴로 들어간 바로가기(학습 계획 만들기). */
private fun homeMenu(state: HomeUiState, actions: HomeActions, onOpenPlanner: () -> Unit): List<AppBarMenuItem> = listOfNotNull(
    if (state.level.shows(StudentHomeSection.RECOMMENDATION)) AppBarMenuItem("영상 저장소", Icons.Default.SmartDisplay, actions.onOpenContent) else null,
) + state.menuShortcuts.mapNotNull { section ->
    if (section == StudentHomeSection.PLANNER) AppBarMenuItem(section.label, Icons.Default.EditCalendar, onOpenPlanner) else null
}
