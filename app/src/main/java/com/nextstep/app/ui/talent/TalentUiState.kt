package com.nextstep.app.ui.talent

import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.model.AptitudeDomain
import com.nextstep.app.domain.insight.AptitudeSignal
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/** 활동·재능 › 재능 섹션. 관찰 메모는 전부, 영역별로 묶어 둡니다(메모가 많은 영역 먼저). */
data class TalentUiState(
    val today: LocalDate = DateUtils.today(),
    val signals: List<AptitudeSignal> = emptyList(),
    val byDomain: List<Pair<AptitudeDomain, List<ObservationEntity>>> = emptyList(),
    val observationCount: Int = 0,
    val loaded: Boolean = false,
)
