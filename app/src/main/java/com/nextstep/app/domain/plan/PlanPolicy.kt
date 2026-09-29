package com.nextstep.app.domain.plan

/**
 * 요금제를 강제할지. 지금은 false 라 모든 기능·한도가 열려 있습니다(PRODUCT_STRATEGY 5장: 출시는 전 기능 무료 + 배너 광고).
 * 유료를 도입할 때는 결제(Play 결제 + 서버 확인)로 [Entitlements.plans] 를 채우고 이 값을 true 로 바꿉니다.
 */
object PlanPolicy {
    const val ENFORCED = false
}
