package com.nextstep.app.ui.journey

import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.JourneyPhase
import com.nextstep.app.domain.journey.MilestoneCategory
import java.time.LocalDate

data class JourneyUiState(
    val studentName: String = "",
    /** 생년월일을 저장할 학생 행. */
    val studentMemberId: String? = null,
    val stage: GrowthStage? = null,
    /** "만 3세 4개월" 같은 나이 표기. 생년월일이 없으면 null. */
    val ageLabel: String? = null,
    val hasBirthDate: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val items: List<JourneyItem> = emptyList(),
    val completion: Float = 0f,
    val filter: MilestoneCategory? = null,
    val showCompleted: Boolean = false,
    val loaded: Boolean = false,
) {
    val filtered: List<JourneyItem> get() = items.filter { filter == null || it.category == filter }

    /** 타임라인 구간별 묶음. 표시 순서는 지난 항목 → 지금 → 다가오는 것 → (옵션) 완료·건너뜀. */
    val sections: List<Pair<JourneyPhase, List<JourneyItem>>> get() = JourneyPhase.entries
        .filter { showCompleted || (it != JourneyPhase.DONE && it != JourneyPhase.SKIPPED) }
        .map { phase -> phase to filtered.filter { it.phase(today) == phase } }
        .filter { it.second.isNotEmpty() }

    val overdueCount: Int get() = items.count { it.phase(today) == JourneyPhase.OVERDUE }
    val nowCount: Int get() = items.count { it.phase(today) == JourneyPhase.NOW }
}
