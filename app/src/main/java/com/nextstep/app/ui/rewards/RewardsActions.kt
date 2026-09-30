package com.nextstep.app.ui.rewards

/** 보상·배지 화면 밖으로 나가는 콜백. */
data class RewardsActions(
    val onOpenGoal: (String) -> Unit = { _ -> },
)
