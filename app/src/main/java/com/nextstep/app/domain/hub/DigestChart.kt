package com.nextstep.app.domain.hub

/**
 * "한눈에" 타일 아래 한 줄 차트. 무엇을 그릴지는 domain 이 정하고 타일은 그리기만 합니다.
 * 과목이 섞인 점수처럼 한 줄로 이으면 뜻이 섞이는 값에는 차트를 붙이지 않습니다.
 */
sealed interface DigestChart {
    /** 작은 막대 줄(오래된 것부터, 마지막이 지금). */
    data class Bars(val values: List<Int>) : DigestChart

    /** 작은 흐름선(오래된 것부터). */
    data class Line(val values: List<Int>) : DigestChart

    /** 0~1 몫 게이지. */
    data class Meter(val fraction: Float) : DigestChart
}
