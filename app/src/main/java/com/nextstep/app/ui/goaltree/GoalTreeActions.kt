package com.nextstep.app.ui.goaltree

/** 목표 화면 밖으로 나가는 콜백. */
data class GoalTreeActions(
    val onOpenGoal: (String) -> Unit = { _ -> },
)
