package com.nextstep.app.domain.reward

/**
 * 보상이 걸린 곳: 목표(목표 id)를 이루면, 레벨(번호)에 닿으면, 스티커판(모두 채운 판 수)을 채우면.
 * 어떤 곳에 걸 수 있는지는 나이에 맞춘 게임 모양이 정합니다([Rewards.kindsFor]).
 */
enum class RewardKind(val label: String) {
    GOAL("목표를 이루면"),
    LEVEL("레벨에 닿으면"),
    BOARD("스티커판을 채우면"),
}
