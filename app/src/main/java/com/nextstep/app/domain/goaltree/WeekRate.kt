package com.nextstep.app.domain.goaltree

import com.nextstep.app.domain.text.percentOf
import com.nextstep.app.domain.text.ratioOrNull
import java.time.LocalDate

/** 한 주(월요일 시작)에 마감이던 할 일 중 끝낸 수. */
data class WeekRate(val weekStart: LocalDate, val due: Int, val done: Int) {
    val rate: Float? get() = ratioOrNull(done, due)

    /** 0~100(마감이던 할 일이 없으면 0). 막대 차트용. */
    val percent: Int get() = percentOf(done, due)
}
