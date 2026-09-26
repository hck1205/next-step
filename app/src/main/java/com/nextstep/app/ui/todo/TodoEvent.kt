package com.nextstep.app.ui.todo

import com.nextstep.app.domain.taskboard.TaskSuggestion

/** 할 일 보드의 사용자 의도. */
sealed interface TodoEvent {
    data class SetFilter(val filter: TodoFilter) : TodoEvent
    data class Toggle(val taskId: String, val done: Boolean) : TodoEvent
    /** 추천을 할 일로. [goalId] 가 있으면 그 목표의 세부 할 일로 넣습니다. */
    data class Accept(val suggestion: TaskSuggestion, val goalId: String?, val createdByRole: String) : TodoEvent
}
