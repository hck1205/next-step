package com.nextstep.app.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.StatTile
import com.nextstep.app.ui.habits.components.DayPartCard
import com.nextstep.app.ui.habits.components.HabitLinesCard
import com.nextstep.app.ui.habits.components.WeekCompareCard
import com.nextstep.app.ui.habits.components.WeekdayCard

/** 공부 › 습관: 문장 요약 → 숫자 네 칸 → 하루 중 언제 → 요일별 → 지난주와 비교. */
@Composable
fun HabitsScreen(viewModel: HabitsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HabitsContent(state)
}

@Composable
internal fun HabitsContent(state: HabitsUiState) {
    val r = state.report ?: return
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { HabitLinesCard(r.lines) }
        if (!r.isEmpty) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("4주 합계", DateUtils.formatMinutes(r.totalMinutes), Modifier.weight(1f))
                    StatTile("공부한 날", "${r.activeDays}일", Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("지금 연속", "${r.currentStreak}일", Modifier.weight(1f), sub = "가장 길게 ${r.longestStreak}일")
                    StatTile("한 번에", "${r.averageSessionMinutes}분", Modifier.weight(1f), sub = "타이머 ${r.timerPercent}%")
                }
            }
            item { DayPartCard(r.byPart, r.bestPart) }
            item { WeekdayCard(r.byWeekday) }
            item { WeekCompareCard(thisWeek = r.thisWeekMinutes, lastWeek = r.lastWeekMinutes) }
        }
    }
}
