package com.nextstep.app.ui.goals

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.domain.journey.GoalTrack
import com.nextstep.app.domain.journey.JourneyPeriod
import java.time.LocalDate

/** 목표 하나와 그 단계들, 진행률, 이번 구간에 할 단계. */
data class GoalView(
    val goal: GoalEntity,
    val steps: List<GoalStepEntity>,
    val progress: Float,
    val currentSteps: List<GoalStepEntity>,
    val isComplete: Boolean,
) {
    val doneCount: Int get() = steps.count { it.status == com.nextstep.app.data.model.MilestoneStatus.DONE }
}

data class GoalsUiState(
    val studentName: String = "",
    val hasBirthDate: Boolean = false,
    val today: LocalDate = LocalDate.now(),
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
