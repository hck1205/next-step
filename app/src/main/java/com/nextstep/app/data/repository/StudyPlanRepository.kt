package com.nextstep.app.data.repository

import com.nextstep.app.domain.planner.StudyPlan

/** 학습 계획 생성기가 만든 일정과 할 일을 한 번에 저장합니다. */
interface StudyPlanRepository {
    suspend fun apply(plan: StudyPlan)
}
