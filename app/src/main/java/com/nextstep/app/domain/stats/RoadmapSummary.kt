package com.nextstep.app.domain.stats

/** 로드맵 항목 수 요약. 대시보드 카드 한 줄의 근거. */
data class RoadmapSummary(val total: Int = 0, val inProgress: Int = 0, val done: Int = 0, val overdue: Int = 0) {
    val isEmpty: Boolean get() = total == 0
}
