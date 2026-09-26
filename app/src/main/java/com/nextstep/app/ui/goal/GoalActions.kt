package com.nextstep.app.ui.goal

/** 목표 화면 밖으로 나가는 콜백. */
data class GoalActions(
    val onBack: () -> Unit = {},
    val onOpenGoal: (String) -> Unit = { _ -> },
)
