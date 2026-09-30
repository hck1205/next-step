package com.nextstep.app.domain.reward

/** 약속 → 받을 차례(이뤘음) → 받음. 받을 차례는 저장하지 않고 목표·레벨에서 계산합니다. */
enum class RewardStatus(val label: String) {
    EARNED("받을 차례"),
    PROMISED("약속"),
    GIVEN("받음"),
}
