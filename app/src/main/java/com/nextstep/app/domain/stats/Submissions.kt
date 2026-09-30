package com.nextstep.app.domain.stats

/** 멘토가 낸 과제의 상태별 수: 끝냄 · 기한 전 · 기한이 지났는데 안 함. */
data class Submissions(val done: Int, val pending: Int, val late: Int) {
    val total: Int get() = done + pending + late
}
