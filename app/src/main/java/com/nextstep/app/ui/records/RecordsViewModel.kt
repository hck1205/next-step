package com.nextstep.app.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.PeriodCalendar
import com.nextstep.app.domain.stats.BalanceStats
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/** 기록 탭의 균형 세그먼트 상태. 다른 세그먼트는 각 기능의 화면이 자기 ViewModel 로 그립니다. */
class RecordsViewModel(
    private val streams: FamilyDataStreams,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    val state: StateFlow<RecordsUiState> = combine(streams.profile, streams.members, streams.sessions, streams.tasks, streams.activities) { profile, members, sessions, tasks, activities ->
        val day = today()
        val stage = GrowthStage.of(members, day)
        val birthDate = members.firstOrNull { it.role == Role.STUDENT.name }?.birthDate?.let { LocalDate.ofEpochDay(it) }
        val period = birthDate?.let { PeriodCalendar.current(it, day) }
        RecordsUiState(
            studentName = profile.studentName,
            stage = stage,
            currentPeriodLabel = period?.label,
            balance = BalanceStats.report(stage, sessions, tasks, activities, period, day),
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordsUiState())
}
