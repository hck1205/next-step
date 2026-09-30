package com.nextstep.app.ui.goaltree

import com.nextstep.app.data.model.GoalStatus

/** 목표 화면의 상태 거르개. */
enum class GoalFilter(val label: String, val status: GoalStatus) {
    ACTIVE("진행 중", GoalStatus.ACTIVE),
    DONE("달성", GoalStatus.DONE),
    ARCHIVED("보관", GoalStatus.ARCHIVED),
}
