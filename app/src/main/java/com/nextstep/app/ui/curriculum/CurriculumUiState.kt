package com.nextstep.app.ui.curriculum

import com.nextstep.app.domain.curriculum.CurriculumPlan
import com.nextstep.app.domain.journey.JourneyPeriod

data class CurriculumUiState(
    val studentName: String = "",
    val hasBirthDate: Boolean = false,
    /** 커리큘럼이 있는 학기 구간만(초1 1학기 ~ 고3 2학기). */
    val periods: List<JourneyPeriod> = emptyList(),
    val currentPeriodKey: String? = null,
    val selectedPeriodKey: String? = null,
    val plan: CurriculumPlan? = null,
    /** 다음 학기 미리 보기: 뼈대 단원 제목만. */
    val nextPreview: List<String> = emptyList(),
    val loaded: Boolean = false,
) {
    val selected: JourneyPeriod? get() = periods.firstOrNull { it.key == selectedPeriodKey }
    val isCurrent: Boolean get() = selectedPeriodKey != null && selectedPeriodKey == currentPeriodKey
    val hasPrev: Boolean get() = periods.indexOfFirst { it.key == selectedPeriodKey } > 0
    val hasNext: Boolean get() = periods.indexOfFirst { it.key == selectedPeriodKey }.let { it >= 0 && it < periods.lastIndex }
}
