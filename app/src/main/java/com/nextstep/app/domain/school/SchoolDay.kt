package com.nextstep.app.domain.school

import java.time.LocalDate

/** NEIS 학사일정 하루 한 줄: 행사 이름, 쉬는 날(휴업일·공휴일)인지, 해당 학년(비면 전 학년). */
data class SchoolDay(val date: LocalDate, val name: String, val dayOff: Boolean = false, val grades: Set<Int> = emptySet())
