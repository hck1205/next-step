package com.nextstep.app.domain.lesson

import java.time.LocalDate

/** 수업이 있는(또는 기록된) 날 하나와 그날의 출결(아직 안 적었으면 null). [extra] 면 정해진 요일이 아닌 날(보강 등). */
data class LessonDay(val date: LocalDate, val status: LessonStatus?, val extra: Boolean = false, val note: String = "")
