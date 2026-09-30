package com.nextstep.app.ui.todo

/** 할 일 보드의 거르개: 전체 · 오늘까지(밀린 것 + 오늘) · 밀린 것만. [empty] 는 걸러서 아무것도 없을 때의 말. */
enum class TodoFilter(val label: String, val empty: String) {
    ALL("전체", "할 일과 추천이 없어요. 목표에서 세부 할 일을 주거나 + 로 할 일을 만들어요."),
    NOW("오늘까지", "오늘까지 할 일이 없어요"),
    OVERDUE("밀린 것", "밀린 할 일이 없어요"),
}
