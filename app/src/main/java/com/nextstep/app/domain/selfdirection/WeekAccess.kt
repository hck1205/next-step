package com.nextstep.app.domain.selfdirection

import com.nextstep.app.domain.access.Capabilities

/** 지금 보는 사람이 이번 주 계획에서 할 수 있는 것. 역할 판단은 [Capabilities], 누가 맡는지는 [SelfDirectionStage] 가 정합니다. */
data class WeekAccess(
    val canPlan: Boolean = false,
    val canCheck: Boolean = false,
    val canApprove: Boolean = false,
    val canReflect: Boolean = false,
    val seesDetails: Boolean = true,
    /** 학생 본인이 보는지(문구: "나의 이번 주"). */
    val forChild: Boolean = false,
) {
    companion object {
        fun of(caps: Capabilities, stage: SelfDirectionStage): WeekAccess = WeekAccess(
            canPlan = caps.canDo(LoopStep.PLAN, stage), canCheck = caps.canDo(LoopStep.CHECK, stage), canApprove = caps.canApproveWeekPlan(stage),
            canReflect = caps.canDo(LoopStep.REFLECT, stage), seesDetails = caps.seesWeekDetails(stage), forChild = caps.isStudent,
        )
    }
}
