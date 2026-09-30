package com.nextstep.app.ui.journey

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.JourneyPeriod

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
