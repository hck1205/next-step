package com.nextstep.app.domain.project

/**
 * 교육 프로젝트 하나: "언제까지 무엇을 할 수 있게"라는 [goal] 을 단계로 나눈 계획.
 * [why] 는 이 순서와 양을 권하는 까닭(근거)입니다. [id] 는 저장된 프로젝트와 연결되므로 바꾸지 않습니다.
 */
data class ProjectPlan(
    val id: String,
    val category: ProjectCategory,
    val title: String,
    val goal: String,
    val span: String,
    val why: String,
    val phases: List<ProjectPhase>,
) {
    val totalHours: Int get() = phases.sumOf { it.totalMinutes } / MINUTES_PER_HOUR
    val startMonths: Int get() = phases.first().fromMonths
    val minDailyMinutes: Int get() = phases.minOf { it.dailyMinutes }
    val maxDailyMinutes: Int get() = phases.maxOf { it.dailyMinutes }

    private companion object {
        const val MINUTES_PER_HOUR = 60
    }
}
