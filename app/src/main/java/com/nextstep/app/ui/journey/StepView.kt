package com.nextstep.app.ui.journey

import com.nextstep.app.data.local.entity.GoalStepEntity

/** 목표 단계 + 목표 제목. 화면은 이것만 봅니다. */
data class StepView(val step: GoalStepEntity, val goalTitle: String)
