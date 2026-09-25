package com.nextstep.app.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AdBanner
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.chart.BarChart
import com.nextstep.app.ui.components.chart.BarItem
import com.nextstep.app.ui.components.chart.DonutChart
import com.nextstep.app.ui.components.chart.HourHeatStrip
import com.nextstep.app.ui.components.card.InsightCard
import com.nextstep.app.ui.components.chart.RadarChart
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.chart.Slice
import com.nextstep.app.ui.components.card.TalentCard
import com.nextstep.app.ui.components.card.subjectColor
import androidx.compose.runtime.getValue

@Composable
fun InsightsScreen(caps: Capabilities, actions: InsightsActions, viewModel: InsightsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    InsightsContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InsightsContent(state: InsightsUiState, caps: Capabilities, actions: InsightsActions, onEvent: (InsightsEvent) -> Unit) {

    // 기록 탭의 세그먼트로 들어가므로 상단 바는 기록 화면이 그립니다.
    Scaffold { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (caps.isParent) {
                item { SectionTitle("재능 발견 · 강점 신호") }
                if (state.talents.isEmpty()) item {
                    AppCard {
                        Text("성적, 학습 시간, 단원 이해도가 쌓이면 효율·성장세·꾸준함·몰입·자기주도성 같은 강점 신호가 여기 표시돼요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(state.talents) { TalentCard(it, state.subjects) }
            }

            item { SectionTitle(if (caps.isParent) "학습 상태 · 제안" else "강점 · 보완점 · 제안") }
            items(state.insights) { insight ->
                InsightCard(insight, state.subjects, onAction = if (caps.canApplyInsightActions) { a -> onEvent(InsightsEvent.ApplyAction(a, caps.actingRoleName)) } else null)
            }

            if (state.scores.size >= 3) {
                item {
                    SectionTitle("과목 균형")
                    AppCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            RadarChart(
                                axes = state.scores.map { it.subject.name },
                                values = state.scores.map { (it.average / 100.0).toFloat() },
                                color = MaterialTheme.colorScheme.primary,
                                secondary = state.progress.filter { p -> state.scores.any { it.subject.id == p.subject.id } }.map { it.myRatio },
                                secondaryColor = MaterialTheme.colorScheme.secondary,
                                chartSize = 240,
                            )
                            Text("보라: 평균 점수 · 초록: 복습 완료율", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                SectionTitle("최근 2주 학습 시간")
                AppCard {
                    BarChart(
                        items = state.daily14.map { d -> BarItem(if (d.date.dayOfMonth % 2 == 1) d.date.dayOfMonth.toString() else "", d.minutes.toFloat(), MaterialTheme.colorScheme.primary) },
                        valueFormatter = { if (it >= 60) "${(it / 60).toInt()}h" else "${it.toInt()}m" },
                        height = 140,
                    )
                }
            }

            item {
                SectionTitle("이번 주 과목별 시간 배분")
                AppCard {
                    DonutChart(
                        slices = state.weeklyBySubject.map { w -> Slice(w.subject?.name ?: "기타", w.minutes.toFloat(), w.subject?.let { subjectColor(it.color) } ?: MaterialTheme.colorScheme.onSurfaceVariant) },
                        centerText = DateUtils.formatMinutes(state.weeklyBySubject.sumOf { it.minutes }),
                    )
                }
            }

            item {
                SectionTitle("시간대별 집중 분포")
                AppCard {
                    Column {
                        HourHeatStrip(state.byHour, MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text("누적 ${DateUtils.formatMinutes(state.totalMinutes)} · 진한 칸일수록 그 시간대에 많이 공부했어요", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (!caps.isStudent) item { AdBanner() }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
