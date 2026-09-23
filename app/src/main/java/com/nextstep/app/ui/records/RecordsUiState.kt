package com.nextstep.app.ui.records

import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.health.GrowthSummary
import com.nextstep.app.domain.insight.AptitudeSignal
import com.nextstep.app.domain.stats.BalanceReport
import java.time.LocalDate

data class RecordsUiState(
    val studentName: String = "",
    val stage: GrowthStage? = null,
    val currentPeriodLabel: String? = null,
    val today: LocalDate = LocalDate.now(),
    val balance: BalanceReport? = null,
    /** 성장 기록 요약(키·몸무게·시력·신호). */
    val growth: GrowthSummary? = null,
    /** 최근 기록 몇 개(최신 먼저). */
    val growthRecords: List<GrowthRecordEntity> = emptyList(),
    /** 예체능·비교과 소질 신호(최대 3개). */
    val aptitude: List<AptitudeSignal> = emptyList(),
    val observations: List<ObservationEntity> = emptyList(),
    val loaded: Boolean = false,
)
