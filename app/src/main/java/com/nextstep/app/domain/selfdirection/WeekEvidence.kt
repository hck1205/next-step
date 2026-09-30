package com.nextstep.app.domain.selfdirection

import java.time.LocalDate

/** 지난 한 주의 자기주도 흔적: 계획이 있었는지, 누가 썼는지, 돌아봤는지, 계획만큼 했는지. */
data class WeekEvidence(
    val weekStart: LocalDate,
    val planned: Boolean,
    val childPlanned: Boolean,
    val reflected: Boolean,
    val goalsDone: Int,
    val goalsTotal: Int,
    /** 계획한 분 대비 실제 공부한 분. 분 계획이 없으면 null. */
    val keptRatio: Float?,
)
