package com.nextstep.app.domain.lesson

import java.time.LocalDate

/** 다음 수업료 받을 날과 남은 날. */
data class TuitionDue(val date: LocalDate, val daysLeft: Int, val fee: Int)
