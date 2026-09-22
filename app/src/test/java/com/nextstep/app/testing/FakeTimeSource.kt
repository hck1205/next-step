package com.nextstep.app.testing

import com.nextstep.app.data.repository.TimeSource

/** 호출마다 1ms 씩 흐르는 고정 시계. `updatedAt` 순서를 결정적으로 검증할 수 있습니다. */
class FakeTimeSource(start: Long = 1_000_000L) : TimeSource {
    var current: Long = start
    override fun now(): Long = current++
}
