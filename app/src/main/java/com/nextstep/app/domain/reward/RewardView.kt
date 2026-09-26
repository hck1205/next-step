package com.nextstep.app.domain.reward

import com.nextstep.app.data.local.entity.RewardEntity

/** 화면에 보이는 보상 한 줄: 무엇을 이루면([target]) 무엇을([RewardEntity.title]), 지금 어디까지 왔는지. */
data class RewardView(val reward: RewardEntity, val kind: RewardKind, val status: RewardStatus, val target: String) {
    /** 언제 받는지 한 줄: "레벨 5에 닿으면" · "\"분수 90점\" 이루면". */
    val condition: String get() = when (kind) {
        RewardKind.LEVEL -> "${target}에 닿으면"
        RewardKind.GOAL -> "\"$target\" 이루면"
    }
}
