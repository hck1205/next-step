package com.nextstep.app.ui.kidme

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.domain.stats.StickerBoard
import com.nextstep.app.domain.gamify.GameProfile
import com.nextstep.app.domain.reward.RewardView
import com.nextstep.app.domain.reward.Rewards
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/**
 * 아이용 "나": 나의 스티커판(한 일마다 한 장 · 특별 스티커), 약속한 선물, 스티커 붙인 날, 이번 주 끝낸 할 일, 최근에 한 것.
 * [game] 은 학부모가 게임 요소를 꺼 두면 null 입니다. [rewards] 는 아직 받지 않은 선물(받을 차례가 먼저).
 */
data class KidMeUiState(
    val studentName: String = "",
    val today: LocalDate = DateUtils.today(),
    val board: StickerBoard? = null,
    val recentActivities: List<ActivityEntity> = emptyList(),
    val loaded: Boolean = false,
    val game: GameProfile? = null,
    val rewards: List<RewardView> = emptyList(),
) {
    val nextReward: RewardView? get() = Rewards.next(rewards)
}
