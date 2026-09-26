package com.nextstep.app.ui.rewards

import com.nextstep.app.domain.reward.RewardKind

/** 보상·배지 화면의 사용자 의도. 약속·주기·취소는 학부모·멘토만 합니다(caps.canGiveRewards). */
sealed interface RewardsEvent {
    data class Promise(val kind: RewardKind, val targetId: String, val title: String) : RewardsEvent
    data class Give(val id: String) : RewardsEvent
    data class Cancel(val id: String) : RewardsEvent
}
