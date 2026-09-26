package com.nextstep.app.domain.goaltree

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.GoalStatus
import java.time.LocalDate

/**
 * 목표 트리의 한 목표: 세부 할 일, 이 목표로 이어지는 작은 목표([children]), 이 목표가 이어지는 큰 목표 길([chain], 가까운 것부터).
 * 달성률 = (끝낸 할 일 + 달성한 작은 목표) ÷ (전체 할 일 + 작은 목표). 작은 목표를 이루면 큰 목표의 달성률이 올라갑니다.
 */
data class GoalNode(
    val goal: GoalEntity,
    /** 끝내지 않은 것 먼저(마감 순), 끝낸 것은 최근에 끝낸 순. */
    val tasks: List<TaskEntity>,
    val children: List<GoalEntity>,
    val chain: List<GoalEntity>,
    val overdue: Int,
    /** 마지막으로 무언가 끝낸 날(없으면 만든 날). */
    val lastActivity: LocalDate,
    val idleDays: Int,
    /** 기한까지 남은 날. 기한이 없으면 null. */
    val daysLeft: Int?,
) {
    val doneTasks: Int get() = tasks.count { it.done }
    val totalTasks: Int get() = tasks.size
    val achievedChildren: Int get() = children.count { it.status == GoalStatus.DONE }
    private val units: Int get() = totalTasks + children.size
    val rate: Float get() = if (units == 0) 0f else (doneTasks + achievedChildren).toFloat() / units
    val isAchieved: Boolean get() = goal.status == GoalStatus.DONE
    val pending: List<TaskEntity> get() = tasks.filter { !it.done }
    /** 할 일과 작은 목표를 모두 끝냈는데 아직 달성으로 표시하지 않은 목표. */
    val readyToAchieve: Boolean get() = !isAchieved && units > 0 && doneTasks == totalTasks && achievedChildren == children.size
    /** 이 목표를 이루면 힘을 보태는 바로 위 목표. */
    val parent: GoalEntity? get() = chain.firstOrNull()
    /** 진행 중인데 [GoalTree.IDLE_DAYS]일 넘게 아무것도 끝내지 않은 목표. */
    val isIdle: Boolean get() = goal.status == GoalStatus.ACTIVE && idleDays >= GoalTree.IDLE_DAYS

    /** 먼저 볼 것: 달성 대기 → 밀린 할 일 → 멈춤 → 비어 있음. 진행 중이 아니면(달성 대기 빼고) 없음. */
    val attention: GoalAttention? get() = when {
        readyToAchieve -> GoalAttention.READY
        goal.status != GoalStatus.ACTIVE -> null
        overdue > 0 -> GoalAttention.OVERDUE
        isIdle -> GoalAttention.IDLE
        units == 0 -> GoalAttention.EMPTY
        else -> null
    }

    /** [attention] 을 한 줄로. */
    val attentionLine: String? get() = when (attention) {
        GoalAttention.READY -> "모두 끝냈어요 · 달성 표시만 남았어요"
        GoalAttention.OVERDUE -> "밀린 할 일 ${overdue}개"
        GoalAttention.IDLE -> "${idleDays}일째 그대로예요"
        GoalAttention.EMPTY -> "세부 할 일을 넣어 시작해요"
        null -> null
    }
}
