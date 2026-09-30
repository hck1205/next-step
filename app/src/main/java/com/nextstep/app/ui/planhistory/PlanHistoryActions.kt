package com.nextstep.app.ui.planhistory

/** 기록 화면 밖으로 나가는 콜백. */
data class PlanHistoryActions(
    val onOpenGoal: (String) -> Unit = { _ -> },
)
