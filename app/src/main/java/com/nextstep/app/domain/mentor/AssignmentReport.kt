package com.nextstep.app.domain.mentor

import com.nextstep.app.data.local.entity.TaskEntity

/**
 * 멘토가 낸 과제의 현황. [overdue] 는 마감이 지났는데 안 한 것, [dueSoon] 은 이번 주 안에 마감인 것.
 * [line] 은 화면 맨 위 한 문장입니다.
 */
data class AssignmentReport(
    val total: Int,
    val done: Int,
    val overdue: List<TaskEntity>,
    val dueSoon: List<TaskEntity>,
    val recentDone: List<TaskEntity>,
    val bySubject: List<AssignmentSubject>,
    val line: String,
) {
    val percent: Int get() = if (total == 0) 0 else done * 100 / total
}
