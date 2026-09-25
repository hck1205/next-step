package com.nextstep.app.ui.growth

import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.domain.health.GrowthSummary
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/** 성장 › 신체 섹션. 기록은 전부, 연도별로 묶어 둡니다(최신 해 먼저). */
data class GrowthUiState(
    val today: LocalDate = DateUtils.today(),
    val summary: GrowthSummary? = null,
    val byYear: List<Pair<Int, List<GrowthRecordEntity>>> = emptyList(),
    /** 키 추이(오래된 것부터, 최근 [GrowthViewModel.TREND_POINTS]개): 차트 라벨과 값. */
    val heightTrend: List<Pair<String, Double>> = emptyList(),
    val recordCount: Int = 0,
    val loaded: Boolean = false,
)
