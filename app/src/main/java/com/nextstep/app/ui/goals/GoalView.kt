package com.nextstep.app.ui.goals

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.mission.MissionKind
import java.time.LocalDate

/**
 * 목표 하나와 그 단계들, 진행률, 이번 구간에 할 단계.
 * 날짜가 정해진 목표(시험·수행평가·입시)면 [target], [daysLeft], [nextStep], [kind] 가 채워집니다.
 */
data class GoalView(
    val goal: GoalEntity,
    val steps: List<GoalStepEntity>,
    val progress: Float,
    val currentSteps: List<GoalStepEntity>,
    val isComplete: Boolean,
    val kind: MissionKind? = null,
    val target: LocalDate? = null,
    val daysLeft: Int? = null,
    val nextStep: GoalStepEntity? = null,
    val overdueSteps: Int = 0,
) {
    val doneCount: Int get() = steps.count { it.status == MilestoneStatus.DONE }
    val isMission: Boolean get() = target != null
}
