package com.nextstep.app.ui.activities

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.domain.journey.ActivitySummary
import com.nextstep.app.domain.journey.JourneyPeriod
import java.time.LocalDate

data class ActivitiesUiState(
    val studentName: String = "",
    val today: LocalDate = LocalDate.now(),
    val periods: List<JourneyPeriod> = emptyList(),
    val currentPeriodKey: String? = null,
    val activities: List<ActivityEntity> = emptyList(),
    val countByType: Map<ActivityType, Int> = emptyMap(),
    val ongoing: List<ActivityEntity> = emptyList(),
    val currentPeriodCount: Int = 0,
    val filter: ActivityType? = null,
    val loaded: Boolean = false,
    /** 구간별 묶음(최근 구간 먼저). ViewModel 이 [sectionsOf] 로 한 번 계산합니다. */
    val sections: List<Pair<String, List<ActivityEntity>>> = emptyList(),
) {
    val filtered: List<ActivityEntity> get() = activities.filter { filter == null || it.type == filter }

    companion object {
        /** 생년월일이 없으면 하나의 묶음. */
        fun sectionsOf(filtered: List<ActivityEntity>, periods: List<JourneyPeriod>): List<Pair<String, List<ActivityEntity>>> {
            if (periods.isEmpty()) return if (filtered.isEmpty()) emptyList() else listOf("전체" to filtered)
            val byKey = ActivitySummary.byPeriod(filtered, periods)
            return periods.asReversed().mapNotNull { p -> byKey[p.key]?.let { p.label to it } }
        }
    }
}
