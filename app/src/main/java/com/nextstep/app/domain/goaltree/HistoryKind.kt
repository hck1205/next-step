package com.nextstep.app.domain.goaltree

/** 기록 타임라인의 사건 종류. */
enum class HistoryKind(val label: String) {
    GOAL_STARTED("목표 시작"),
    TASK_DONE("할 일 끝"),
    GOAL_ACHIEVED("목표 달성"),
}
