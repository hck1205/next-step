package com.nextstep.app.ui.goals

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.MilestoneStatus

/** 목표 하나와 그 단계들, 진행률, 이번 구간에 할 단계. */
data class GoalView(
    val goal: GoalEntity,
    val steps: List<GoalStepEntity>,
    val progress: Float,
    val currentSteps: List<GoalStepEntity>,
    val isComplete: Boolean,
) {
    val doneCount: Int get() = steps.count { it.status == MilestoneStatus.DONE }
}
