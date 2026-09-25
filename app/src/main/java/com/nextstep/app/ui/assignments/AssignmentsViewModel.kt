package com.nextstep.app.ui.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.mentor.AssignmentStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** 과제·피드백 › 과제. 과제를 내는 것은 + 시트, 끝내는 것은 학생의 오늘 화면이 맡고 여기서는 현황만 봅니다. */
class AssignmentsViewModel(
    streams: FamilyDataStreams,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    val state: StateFlow<AssignmentsUiState> = combine(streams.tasks, streams.subjects) { tasks, subjects ->
        val day = today()
        AssignmentsUiState(report = AssignmentStats.report(tasks, subjects, day), subjects = subjects, today = day, loaded = true)
    }.asUiState(viewModelScope, AssignmentsUiState())
}
