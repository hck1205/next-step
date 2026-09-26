package com.nextstep.app.ui.selfdirection

import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.domain.selfdirection.SelfDirectionReport
import com.nextstep.app.domain.selfdirection.WeekStatus

/** 기록 › 공부 › 스스로: 자기주도 사다리의 지금 자리, 이번 주, 최근 4주 흔적, 지난 주들의 계획과 돌아보기. */
data class SelfDirectionUiState(
    val loaded: Boolean = false,
    val studentName: String = "",
    val studentId: String? = null,
    val report: SelfDirectionReport? = null,
    val week: WeekStatus? = null,
    /** 이번 주를 뺀 지난 주들(최근 주가 먼저). */
    val history: List<WeekPlanEntity> = emptyList(),
)
