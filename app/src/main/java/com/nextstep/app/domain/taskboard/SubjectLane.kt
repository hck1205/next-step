package com.nextstep.app.domain.taskboard

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity

/**
 * 할 일 보드의 한 줄(과목 하나, [subject] 가 null 이면 과목 밖 = 목표·생활 할 일).
 * 밀린 것 → 오늘 → 다가오는 것 순서, 이번 주 끝낸 수, 이 과목의 추천(3개까지), 최근 4주 달성률.
 */
data class SubjectLane(
    val subject: SubjectEntity?,
    val overdue: List<TaskEntity>,
    val today: List<TaskEntity>,
    val upcoming: List<TaskEntity>,
    val doneThisWeek: Int,
    val suggestions: List<TaskSuggestion>,
    val recentRate: Float?,
) {
    val pendingCount: Int get() = overdue.size + today.size + upcoming.size
    val isEmpty: Boolean get() = pendingCount == 0 && suggestions.isEmpty() && doneThisWeek == 0
}
