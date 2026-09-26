package com.nextstep.app.ui.todo

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.domain.taskboard.SubjectLane

/**
 * 기록 › 목표·할 일 › 할 일: 과목별 줄(밀린 것 → 오늘 → 다가오는 것 + 그 과목의 추천). [goals] 는 진행 중인 목표(추천을 목표에 넣을 때, 할 일에 목표 이름 표시).
 */
data class TodoUiState(
    val loaded: Boolean = false,
    val lanes: List<SubjectLane> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val filter: TodoFilter = TodoFilter.ALL,
    val stage: SelfDirectionStage = SelfDirectionStage.OWN,
) {
    val overdueCount: Int get() = lanes.sumOf { it.overdue.size }
    val todayCount: Int get() = lanes.sumOf { it.today.size }
    val doneThisWeek: Int get() = lanes.sumOf { it.doneThisWeek }
    val suggestionCount: Int get() = lanes.sumOf { it.suggestions.size }
    val shown: List<SubjectLane> get() = when (filter) {
        TodoFilter.ALL -> lanes
        TodoFilter.NOW -> lanes.filter { it.overdue.isNotEmpty() || it.today.isNotEmpty() }
        TodoFilter.OVERDUE -> lanes.filter { it.overdue.isNotEmpty() }
    }
    fun goalTitle(id: String?): String? = id?.let { g -> goals.firstOrNull { it.id == g }?.title }
}
