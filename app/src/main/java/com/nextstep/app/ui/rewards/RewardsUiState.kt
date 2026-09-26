package com.nextstep.app.ui.rewards

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.domain.gamify.GameProfile
import com.nextstep.app.domain.reward.RewardStatus
import com.nextstep.app.domain.reward.RewardView
import com.nextstep.app.domain.reward.Rewards

/**
 * 기록 › 목표·할 일 › 보상·배지: 레벨·배지·경험치 내역(게임 요소가 켜져 있을 때)과 보상 목록.
 * [goals] 는 보상을 걸 수 있는 진행 중인 목표입니다.
 */
data class RewardsUiState(
    val loaded: Boolean = false,
    val gamify: Boolean = true,
    val profile: GameProfile = GameProfile.EMPTY,
    val rewards: List<RewardView> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
) {
    val due: List<RewardView> get() = rewards.filter { it.status == RewardStatus.EARNED }
    val promised: List<RewardView> get() = rewards.filter { it.status == RewardStatus.PROMISED }
    val given: List<RewardView> get() = rewards.filter { it.status == RewardStatus.GIVEN }
    val nextReward: RewardView? get() = Rewards.next(rewards)
    /** 게임 요소가 꺼져 있으면 레벨 보상은 약속하지 않습니다(아이에게 레벨이 보이지 않으므로). */
    val levelChoices: List<Int> get() = if (gamify) Rewards.levelChoices(profile.level.number) else emptyList()
    val canPromise: Boolean get() = goals.isNotEmpty() || levelChoices.isNotEmpty()
}
