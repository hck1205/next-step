package com.nextstep.app.data.plan

import com.nextstep.app.domain.plan.Plan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** 결제를 붙이기 전: 산 요금제가 없고, PlanPolicy.ENFORCED = false 라 모든 기능이 열려 있습니다. */
class OpenPlanRepository : PlanRepository {
    override val plans: Flow<Set<Plan>> = flowOf(emptySet())
}
