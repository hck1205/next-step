package com.nextstep.app.ui.selfdirection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.selfdirection.WeekAccess
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.card.WeekPlanCard
import com.nextstep.app.ui.selfdirection.components.EvidenceCard
import com.nextstep.app.ui.selfdirection.components.LadderCard
import com.nextstep.app.ui.selfdirection.components.LoopCard
import com.nextstep.app.ui.selfdirection.components.PastWeekCard
import com.nextstep.app.ui.selfdirection.components.SuggestionCard

/**
 * 기록 › 공부 › 스스로. 자기주도 사다리(어른이 → 같이 → 스스로)의 지금 자리와, 계획 · 실행 · 점검 · 돌아보기를 누가 맡는지,
 * 이번 주 계획, 최근 4주 흔적, 한 칸 맡길 준비가 됐는지(제안), 지난 주들의 계획과 돌아보기를 보여 줍니다.
 */
@Composable
fun SelfDirectionScreen(caps: Capabilities, actions: SelfDirectionActions, viewModel: SelfDirectionViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SelfDirectionContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@Composable
internal fun SelfDirectionContent(state: SelfDirectionUiState, caps: Capabilities, actions: SelfDirectionActions, onEvent: (SelfDirectionEvent) -> Unit) {
    val report = state.report ?: return
    val access = WeekAccess.of(caps, report.stage)
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            LadderCard(
                report = report, forChild = caps.isStudent,
                onChoose = if (caps.canChooseSelfDirection) ({ onEvent(SelfDirectionEvent.SetStage(it)) }) else null,
            )
        }
        report.suggestion?.let { s ->
            item { SuggestionCard(s, onAccept = if (caps.canChooseSelfDirection) ({ onEvent(SelfDirectionEvent.SetStage(s.to)) }) else null) }
        }
        item { LoopCard(report.stage) }
        state.week?.let { week ->
            item {
                WeekPlanCard(
                    week = week, access = access,
                    onSavePlan = { goals, minutes -> onEvent(SelfDirectionEvent.SavePlan(goals, minutes)) },
                    onToggle = { id, i -> onEvent(SelfDirectionEvent.ToggleGoal(id, i)) },
                    onApprove = { onEvent(SelfDirectionEvent.Approve(it)) },
                    onReflect = { w, mood, good, hard, change -> onEvent(SelfDirectionEvent.Reflect(w, mood, good, hard, change)) },
                )
            }
        }
        item { EvidenceCard(report) }
        if (access.seesDetails && state.history.isNotEmpty()) {
            item { SectionTitle("지난 주들 · ${state.history.size}") }
            items(state.history, key = { it.id }) { PastWeekCard(it) }
        }
    }
}
