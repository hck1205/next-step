package com.nextstep.app.ui.records

import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.stats.BalanceReport

data class RecordsUiState(
    val studentName: String = "",
    val stage: GrowthStage? = null,
    val currentPeriodLabel: String? = null,
    val balance: BalanceReport? = null,
    val loaded: Boolean = false,
)
