package com.nextstep.app.domain.stats

import java.time.LocalDate

/** 공부 달력의 하루. [level] 은 0(안 함)~4(45분 이상), [future] 는 아직 오지 않은 날. */
data class HeatDay(val date: LocalDate, val minutes: Int, val level: Int, val future: Boolean)
