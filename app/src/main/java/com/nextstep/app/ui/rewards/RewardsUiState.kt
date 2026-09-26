package com.nextstep.app.ui.rewards

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.domain.gamify.GameProfile
import com.nextstep.app.domain.gamify.GameStyle
import com.nextstep.app.domain.reward.RewardStatus
import com.nextstep.app.domain.reward.RewardTarget
import com.nextstep.app.domain.reward.RewardView
import com.nextstep.app.domain.reward.Rewards

/**
 * 기록 › 목표·할 일 › 보상·배지: 스티커판·레벨·성장 기록(게임 요소가 켜져 있을 때, 모양은 나이에 맞춘 [style])과 보상 목록.
 * [goals] 는 보상을 걸 수 있는 진행 중인 목표입니다.
 */
data class RewardsUiState(
    val loaded: Boolean = false,
    val gamify: Boolean = true,
    val style: GameStyle = GameStyle.LEVELS,
    val profile: GameProfile = GameProfile.EMPTY,
    val rewards: List<RewardView> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
) {
    val due: List<RewardView> get() = rewards.filter { it.status == RewardStatus.EARNED }
    val promised: List<RewardView> get() = rewards.filter { it.status == RewardStatus.PROMISED }
    val given: List<RewardView> get() = rewards.filter { it.status == RewardStatus.GIVEN }
    val nextReward: RewardView? get() = Rewards.next(rewards)
    /** 이 나이에 보상을 걸 수 있는 곳(게임 요소가 꺼져 있으면 목표만). */
    val targets: List<RewardTarget> get() = Rewards.targets(style, gamify, profile, goals)
    val canPromise: Boolean get() = targets.isNotEmpty()
    val ideas: List<String> get() = Rewards.ideasFor(style)
    val hint: String get() = Rewards.hintFor(style)
}
