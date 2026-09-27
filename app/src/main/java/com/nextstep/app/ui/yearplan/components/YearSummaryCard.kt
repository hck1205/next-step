package com.nextstep.app.ui.yearplan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.yearplan.YearPlanUiState

/** 올해의 주제 한 줄과 기본 진행 막대. 앞서 가기는 막대에 넣지 않고 "여유가 있을 때만" 한 줄로 따로 셉니다. */
@Composable
internal fun YearSummaryCard(state: YearPlanUiState, theme: String) {
    val numbers = state.level.showsNumbers
    AppCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(theme, style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(progress = { if (state.total == 0) 0f else state.done.toFloat() / state.total }, modifier = Modifier.fillMaxWidth())
            Text(
                if (numbers) "기본 ${state.done} / ${state.total} 끝냈어요 · 지금 ${state.currentTerm.label}" else "별 ${state.done}개 모았어요",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.aheadTotal > 0) {
                Text(
                    state.aheadHeading + (if (numbers) " ${state.aheadDone} / ${state.aheadTotal}" else "") + " · 여유가 있을 때만",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}
