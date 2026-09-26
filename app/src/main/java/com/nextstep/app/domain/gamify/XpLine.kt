package com.nextstep.app.domain.gamify

/** 경험치 내역 한 줄: 어디서 몇 번, 그래서 몇 점. */
data class XpLine(val source: XpSource, val count: Int) {
    val xp: Int get() = source.xp * count
}
