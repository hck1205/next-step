package com.nextstep.app.domain.stats

import java.time.LocalDate

/** 한 주(월요일 시작)에 마감인 할 일 중 끝낸 몫. */
data class WeekRate(val monday: LocalDate, val total: Int, val done: Int) {
    /** 0~100. 할 일이 없던 주는 0. */
    val percent: Int get() = if (total == 0) 0 else done * PERCENT / total

    private companion object { const val PERCENT = 100 }
}
