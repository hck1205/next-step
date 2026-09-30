package com.nextstep.app.ui.todo

/** 할 일 보드 밖으로 나가는 콜백. */
data class TodoActions(
    val onOpenGoal: (String) -> Unit = { _ -> },
    val onOpenSubject: (String) -> Unit = { _ -> },
)
