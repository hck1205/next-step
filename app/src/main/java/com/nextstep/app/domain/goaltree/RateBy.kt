package com.nextstep.app.domain.goaltree

/** 묶음(누가 준 할 일 · 과목)별 달성: 끝낸 수 / 전체. */
data class RateBy(val key: String, val label: String, val done: Int, val total: Int) {
    val rate: Float get() = if (total == 0) 0f else done.toFloat() / total
}
