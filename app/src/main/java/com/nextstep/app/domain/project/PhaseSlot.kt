package com.nextstep.app.domain.project

import java.time.LocalDate

/** 일정 위의 단계 하나: 계획한 시작·끝 날짜. 이미 할 수 있어 건너뛴 단계는 날짜가 없습니다. */
data class PhaseSlot(val index: Int, val phase: ProjectPhase, val start: LocalDate?, val end: LocalDate?)
