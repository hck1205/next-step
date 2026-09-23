package com.nextstep.app.ui.journey

import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.JourneyPeriod
import com.nextstep.app.domain.journey.JourneyPhase
import com.nextstep.app.domain.journey.MilestoneCategory
import java.time.LocalDate

/** 구간(학기) 하나의 타임라인 칸: 그 구간에 마감이 있는 이정표와 그 구간에 배정된 목표 단계. */
data class PeriodSection(
    val period: JourneyPeriod,
    val isCurrent: Boolean,
    val isPast: Boolean,
    val milestones: List<JourneyItem>,
    val steps: List<StepView>,
    /** 그 구간에 시작한 활동 기록. */
    val activities: List<ActivityEntity> = emptyList(),
)

/** 목표 단계 + 목표 제목. 화면은 이것만 봅니다. */
data class StepView(val step: GoalStepEntity, val goalTitle: String)

data class JourneyUiState(
    val studentName: String = "",
    /** 생년월일을 저장할 학생 행. */
    val studentMemberId: String? = null,
    val stage: GrowthStage? = null,
    /** "만 3세 4개월" 같은 나이 표기. 생년월일이 없으면 null. */
    val ageLabel: String? = null,
    val hasBirthDate: Boolean = false,
    val today: LocalDate = DateUtils.today(),
    val items: List<JourneyItem> = emptyList(),
    val periods: List<JourneyPeriod> = emptyList(),
    val currentPeriodKey: String? = null,
    val goals: List<GoalEntity> = emptyList(),
    val steps: List<GoalStepEntity> = emptyList(),
    val activities: List<ActivityEntity> = emptyList(),
    /** 현재 구간의 교과 커리큘럼(학령기만). */
    val curriculum: com.nextstep.app.domain.curriculum.TermCurriculum? = null,
    val completion: Float = 0f,
    val filter: MilestoneCategory? = null,
    val showCompleted: Boolean = false,
    /** 지난 구간까지 펼쳐 보기. 기본은 현재 구간부터. */
    val showPast: Boolean = false,
    val loaded: Boolean = false,
    /** 아래 세 값은 ViewModel 이 JourneySections 로 한 번 계산합니다(리컴포지션마다 재계산하지 않음). */
    val periodSections: List<PeriodSection> = emptyList(),
    val phaseSections: List<Pair<JourneyPhase, List<JourneyItem>>> = emptyList(),
    val overdueCount: Int = 0,
    val nowCount: Int = 0,
    val pastSectionCount: Int = 0,
) {
    val filtered: List<JourneyItem> get() = items.filter { filter == null || it.category == filter }
}
