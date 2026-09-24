package com.nextstep.app.domain.mission

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity

/** 오늘 화면의 "다음 한 걸음": 날짜가 정해진 목표, 다음 단계, 남은 날, 밀린 단계 수, 진행률. */
data class MissionFocus(
    val goal: GoalEntity,
    val kind: MissionKind?,
    val nextStep: GoalStepEntity,
    val daysLeft: Int,
    val overdueSteps: Int,
    val progress: Float,
    val stepCount: Int,
    val doneCount: Int,
)
