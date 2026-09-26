package com.nextstep.app.domain.reward

/** 보상이 걸린 곳: 목표(목표 id)를 이루면, 또는 레벨(번호)에 닿으면. */
enum class RewardKind(val label: String) {
    GOAL("목표를 이루면"),
    LEVEL("레벨에 닿으면"),
}
