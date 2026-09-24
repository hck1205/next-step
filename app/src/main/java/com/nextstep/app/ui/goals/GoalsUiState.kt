package com.nextstep.app.ui.goals

import com.nextstep.app.domain.mission.MissionKind
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
    /** 날짜가 정해진 진행 중 목표. 목표일이 가까운 순서. */
    val missions: List<GoalView> = emptyList(),
    /** 지금 성장 단계에서 만들 수 있는 날짜 목표 종류. */
    val missionKinds: List<MissionKind> = emptyList(),
    /** 과목 이름(수행평가·단원평가 제목용). */
    val subjectNames: List<String> = emptyList(),
    val loaded: Boolean = false,
) {
    val active: List<GoalView> get() = goals.filter { it.goal.status == GoalStatus.ACTIVE && !it.isMission }
    val finished: List<GoalView> get() = goals.filter { it.goal.status != GoalStatus.ACTIVE }
    val currentPeriodLabel: String? get() = periods.firstOrNull { it.key == currentPeriodKey }?.label
    fun periodLabel(key: String): String = periods.firstOrNull { it.key == key }?.label ?: key
}
