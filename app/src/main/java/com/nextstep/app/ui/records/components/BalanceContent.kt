package com.nextstep.app.ui.records.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.BalanceVerdict
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.records.RecordsActions
import com.nextstep.app.ui.records.RecordsUiState
import com.nextstep.app.ui.records.RecordsEvent
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.domain.access.Capabilities
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/** 균형 세그먼트: 학습·자기주도·경험 게이지와 한 줄 판단. 또래 비교는 없습니다. */
@Composable
internal fun BalanceContent(state: RecordsUiState, caps: Capabilities, actions: RecordsActions, onEvent: (RecordsEvent) -> Unit, modifier: Modifier = Modifier) {
    val b = state.balance
    var editGrowth by remember { mutableStateOf<GrowthRecordEntity?>(null) }
    var showGrowth by remember { mutableStateOf(false) }
    var showObserve by remember { mutableStateOf(false) }
    if (showGrowth) GrowthRecordDialog(existing = editGrowth, today = state.today, onConfirm = { onEvent(RecordsEvent.SaveGrowth(it)); showGrowth = false }, onDismiss = { showGrowth = false })
    if (showObserve) ObservationDialog(today = state.today, onConfirm = { onEvent(RecordsEvent.AddObservation(it)); showObserve = false }, onDismiss = { showObserve = false })
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (b == null) {
            item { AppCard { EmptyState("기록이 쌓이면 균형을 보여 드려요") } }
            return@LazyColumn
        }
        item {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(b.headline, style = MaterialTheme.typography.titleMedium)
                    val ratio = if (b.recommendedWeekMinutes == 0) 0f else (b.weekMinutes.toFloat() / b.recommendedWeekMinutes).coerceIn(0f, 1f)
                    LabeledProgress(
                        label = "학습 · ${DateUtils.formatMinutes(b.weekMinutes)}",
                        ratio = ratio,
                        color = if (b.studyVerdict == BalanceVerdict.LESS) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trailing = b.studyVerdict.label,
                    )
                    Text(
                        if (b.recommendedWeekMinutes == 0) b.studyLine else "${b.studyLine} · ${state.stage?.label ?: ""} 권장 주 ${DateUtils.formatMinutes(b.recommendedWeekMinutes)}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val self = b.selfDirectedRatio
                    LabeledProgress(label = "스스로 만든 계획", ratio = self ?: 0f, color = MaterialTheme.colorScheme.tertiary, trailing = self?.let { "${(it * 100).toInt()}%" } ?: "아직 없음")
                    Text(
                        when {
                            self == null -> "할 일이 쌓이면 학생이 스스로 만든 비율을 보여 드려요"
                            self >= 0.6f -> "스스로 계획하는 힘이 자라고 있어요"
                            else -> "어른이 만든 할 일이 더 많아요. 아이가 하나라도 직접 정하게 해 보세요"
                        },
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            AppCard(onClick = actions.onOpenActivities) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("경험 · ${state.currentPeriodLabel ?: "이번 구간"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (b.experiencesThisPeriod == 0) "아직 기록한 활동이 없어요" else "활동 ${b.experiencesThisPeriod}개를 기록했어요", style = MaterialTheme.typography.titleSmall)
                    Text("현장학습·취미·동아리는 성적만큼 중요한 기록이에요. 눌러서 남겨 보세요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            GrowthCard(
                summary = state.growth, records = state.growthRecords,
                onAdd = if (caps.canRecordGrowth) ({ editGrowth = null; showGrowth = true }) else null,
                onEdit = { editGrowth = it; showGrowth = true },
                onDelete = if (caps.canRecordGrowth) ({ onEvent(RecordsEvent.DeleteGrowth(it)) }) else null,
            )
        }
        item {
            AptitudeCard(
                signals = state.aptitude, observations = state.observations,
                onObserve = if (caps.canRecordGrowth) ({ showObserve = true }) else null,
                onDeleteObservation = if (caps.canRecordGrowth) ({ onEvent(RecordsEvent.DeleteObservation(it)) }) else null,
            )
        }
        item {
            AppCard(onClick = actions.onOpenJourney) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("연속 학습", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (b.streak == 0) "오늘 첫 기록을 남기면 시작돼요" else "${b.streak}일째 이어지고 있어요", style = MaterialTheme.typography.titleSmall)
                    Text("비교 대상은 다른 아이가 아니라 지난주의 우리 아이예요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
