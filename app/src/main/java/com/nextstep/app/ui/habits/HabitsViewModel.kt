package com.nextstep.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.stats.StudyHabits
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** 공부 › 습관. 학생·학부모·멘토가 같은 계산을 봅니다(비교는 지난 기록과만). */
class HabitsViewModel(
    streams: FamilyDataStreams,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    val state: StateFlow<HabitsUiState> = streams.sessions.map { sessions ->
        HabitsUiState(report = StudyHabits.report(sessions, today()), loaded = true)
    }.asUiState(viewModelScope, HabitsUiState())
}
