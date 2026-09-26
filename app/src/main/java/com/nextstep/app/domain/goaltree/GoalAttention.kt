package com.nextstep.app.domain.goaltree

/** 목표에서 먼저 볼 것 한 가지. 목표 카드 · 오늘의 목표 진행 · 한눈에 타일이 같은 기준을 씁니다. */
enum class GoalAttention {
    /** 할 일과 작은 목표를 모두 끝냈고 달성 표시만 남음. */
    READY,
    /** 마감이 지난 세부 할 일이 있음. */
    OVERDUE,
    /** [GoalTree.IDLE_DAYS]일 넘게 아무것도 끝내지 않음. */
    IDLE,
    /** 아직 세부 할 일도 작은 목표도 없음. */
    EMPTY,
}
