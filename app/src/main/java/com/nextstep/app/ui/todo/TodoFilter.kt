package com.nextstep.app.ui.todo

/** 할 일 보드의 거르개: 전체 · 오늘까지(밀린 것 + 오늘) · 밀린 것만. */
enum class TodoFilter(val label: String) {
    ALL("전체"),
    NOW("오늘까지"),
    OVERDUE("밀린 것"),
}
