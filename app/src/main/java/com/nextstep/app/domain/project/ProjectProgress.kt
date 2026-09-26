package com.nextstep.app.domain.project

import java.time.LocalDate

/**
 * 진행 중인 프로젝트 하나의 지금 모습. [currentIndex] 는 아직 통과하지 않은 첫 단계(모두 통과했으면 단계 수와 같음).
 * 이번 주 [weekMinutes] 는 기록한 루틴의 합, [weekTarget] 은 지금 단계의 주간 권장량입니다.
 */
data class ProjectProgress(
    val goalId: String,
    val plan: ProjectPlan,
    val currentIndex: Int,
    val passed: Int,
    val weekMinutes: Int,
    val weekTarget: Int,
    val todayMinutes: Int,
    val todayDoneItems: Set<String>,
    val activeDaysThisWeek: Int,
    val pace: ProjectPace,
    val targetDate: LocalDate?,
    val projectedEnd: LocalDate?,
) {
    val total: Int get() = plan.phases.size
    val current: ProjectPhase? get() = plan.phases.getOrNull(currentIndex)
    val isDone: Boolean get() = currentIndex >= total
    val next: ProjectPhase? get() = plan.phases.getOrNull(currentIndex + 1)
}
