package com.nextstep.app.ui.goal

import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.journey.GoalArea
import java.time.LocalDate

/** 목표 화면의 사용자 의도. [createdByRole] 은 누가 준 할 일·목표인지로 남습니다. */
sealed interface GoalEvent {
    data class AddTask(val title: String, val subjectId: String?, val type: TaskType, val due: LocalDate, val createdByRole: String) : GoalEvent
    data class ToggleTask(val taskId: String, val done: Boolean) : GoalEvent
    data class DeleteTask(val taskId: String) : GoalEvent
    /** 달성으로 표시. 이어지는 목표의 달성률이 오릅니다. */
    data object Achieve : GoalEvent
    data object Reopen : GoalEvent
    data object Archive : GoalEvent
    data class Link(val leadsTo: String?) : GoalEvent
    data class Edit(val title: String, val why: String, val target: LocalDate?) : GoalEvent
    /** 이 목표로 이어지는 작은 목표 만들기. */
    data class AddChild(val title: String, val why: String, val area: GoalArea, val target: LocalDate?, val createdByRole: String) : GoalEvent
    /** 달성한 뒤 다음 목표 만들기: 같은 큰 목표로 이어집니다. */
    data class AddNext(val title: String, val why: String, val area: GoalArea, val target: LocalDate?, val createdByRole: String) : GoalEvent
}
