package com.nextstep.app.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AdBanner
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.InsightCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.card.SubjectRadarCard
import com.nextstep.app.ui.components.card.TalentCard
import com.nextstep.app.ui.components.layout.ScreenPadding
import com.nextstep.app.ui.insights.components.FocusHoursCard
import com.nextstep.app.ui.insights.components.StudyDaysCard
import com.nextstep.app.ui.insights.components.SubjectShareCard

@Composable
fun InsightsScreen(caps: Capabilities, viewModel: InsightsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    InsightsContent(state = state, caps = caps, onEvent = viewModel::onEvent)
}

/** 분석: (학부모) 재능 발견 → 강점·보완점·제안 → 과목 균형 · 2주 시간 · 과목 배분 · 시간대. */
@Composable
internal fun InsightsContent(state: InsightsUiState, caps: Capabilities, onEvent: (InsightsEvent) -> Unit) {
    // 기록 탭의 세그먼트로 들어가므로 상단 바는 기록 화면이 그립니다.
    Scaffold { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenPadding.list,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (caps.isParent) talents(state)
            item { SectionTitle(if (caps.isParent) "학습 상태 · 제안" else "강점 · 보완점 · 제안") }
            items(state.insights) { insight ->
                InsightCard(insight, state.subjects, onAction = if (caps.canCreateTasks) { a -> onEvent(InsightsEvent.ApplyAction(a)) } else null)
            }
            charts(state)
            if (!caps.isStudent) item { AdBanner() }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/** 재능 발견(학부모): 쌓인 기록에서 찾은 강점 신호. */
private fun LazyListScope.talents(state: InsightsUiState) {
    item { SectionTitle("재능 발견 · 강점 신호") }
    if (state.talents.isEmpty()) item {
        AppCard {
            Text("성적, 학습 시간, 단원 이해도가 쌓이면 효율·성장세·꾸준함·몰입·자기주도성 같은 강점 신호가 여기 표시돼요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    items(state.talents) { TalentCard(it, state.subjects) }
}

/** 차트: 과목 균형(과목이 셋 이상일 때) · 최근 2주 · 이번 주 배분 · 시간대. */
private fun LazyListScope.charts(state: InsightsUiState) {
    if (state.scores.size >= MIN_RADAR_SUBJECTS) item {
        SectionTitle("과목 균형")
        SubjectRadarCard(state.scores, reviewRatios = state.reviewRatios)
    }
    item {
        SectionTitle("최근 2주 학습 시간")
        StudyDaysCard(state.daily14)
    }
    item {
        SectionTitle("이번 주 과목별 시간 배분")
        SubjectShareCard(state.weeklyBySubject)
    }
    item {
        SectionTitle("시간대별 집중 분포")
        FocusHoursCard(state.byHour, state.totalMinutes)
    }
}

private const val MIN_RADAR_SUBJECTS = 3
