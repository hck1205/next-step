package com.nextstep.app.data.plan

import com.nextstep.app.domain.plan.Plan
import kotlinx.coroutines.flow.Flow

/** 이 사용자가 산 요금제. 결제(Play 결제 + 서버 확인)를 붙일 자리이고, 지금은 [OpenPlanRepository](빈 목록 — 모두 열림)입니다. */
interface PlanRepository {
    val plans: Flow<Set<Plan>>
}
