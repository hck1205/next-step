package com.nextstep.app.ui.goals

import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.domain.journey.GoalTrack
import com.nextstep.app.domain.journey.JourneyPeriod
import java.time.LocalDate

data class GoalsUiState(
    val studentName: String = "",
    val hasBirthDate: Boolean = false,
    val today: LocalDate = DateUtils.today(),
    val periods: List<JourneyPeriod> = emptyList(),
    val currentPeriodKey: String? = null,
    val goals: List<GoalView> = emptyList(),
    /** 아직 시작하지 않았고 지금 이후 구간에 단계가 있는 트랙. */
    val availableTracks: List<GoalTrack> = emptyList(),
    val loaded: Boolean = false,
) {
    val active: List<GoalView> get() = goals.filter { it.goal.status == GoalStatus.ACTIVE }
    val finished: List<GoalView> get() = goals.filter { it.goal.status != GoalStatus.ACTIVE }
    val currentPeriodLabel: String? get() = periods.firstOrNull { it.key == currentPeriodKey }?.label
    fun periodLabel(key: String): String = periods.firstOrNull { it.key == key }?.label ?: key
}
