package com.nextstep.app.domain.plan

/**
 * 이 사용자가 쓸 수 있는 것: 가진 요금제([plans])와 강제 여부([enforced]). 강제하지 않으면 모든 기능·한도가 열립니다.
 * 광고 없음은 "기능을 연다"가 아니라 "광고를 뺀다"라서, 강제할 때 가족 플러스가 있을 때만 참입니다([adFree]).
 */
data class Entitlements(val plans: Set<Plan> = emptySet(), val enforced: Boolean = PlanPolicy.ENFORCED) {
    fun has(feature: Feature): Boolean = !enforced || feature.plan == Plan.FREE || feature.plan in plans

    /** 개수 한도(열려 있으면 [Int.MAX_VALUE]). */
    fun limit(limit: Limit): Int = if (!enforced || limit.plan in plans) Int.MAX_VALUE else limit.free

    val adFree: Boolean get() = enforced && Plan.FAMILY_PLUS in plans

    companion object {
        /** 결제를 붙이기 전의 기본값: 모두 열림. */
        val OPEN = Entitlements()
    }
}
