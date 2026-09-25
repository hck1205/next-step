package com.nextstep.app.ui.overview

import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.hub.ConcernDigest
import com.nextstep.app.domain.stats.BalanceReport
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/** 기록 › 한눈에: 균형 판단 한 장과 관심사마다 타일 한 장. */
data class OverviewUiState(
    val studentName: String = "",
    val stage: GrowthStage? = null,
    val currentPeriodLabel: String? = null,
    /** 올해 표기(예: 초3, 만 4세). 권장선 문장에 씁니다. */
    val yearLabel: String? = null,
    val today: LocalDate = DateUtils.today(),
    val balance: BalanceReport? = null,
    /** 공부 · 시험·성적 · 성장 · 활동·재능 순서. */
    val digests: List<ConcernDigest> = emptyList(),
    val loaded: Boolean = false,
)
