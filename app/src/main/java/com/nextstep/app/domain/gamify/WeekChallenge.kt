package com.nextstep.app.domain.gamify

/** 이번 주 도전 한 가지: [label] 을 [target] 만큼, 지금 [done]. 매주 월요일에 새로 시작합니다. */
data class WeekChallenge(val label: String, val done: Int, val target: Int) {
    val complete: Boolean get() = done >= target
    val ratio: Float get() = (done.toFloat() / target).coerceIn(0f, 1f)
}
