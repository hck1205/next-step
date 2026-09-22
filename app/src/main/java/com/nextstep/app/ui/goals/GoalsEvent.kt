package com.nextstep.app.ui.goals

import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.journey.GoalArea

/** Goals 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface GoalsEvent {
    data class StartTrack(val trackId: String) : GoalsEvent
    /** 직접 만든 목표: 제목과 구간별 단계 제목(비어 있으면 단계 없이 시작). */
    data class AddCustomGoal(val title: String, val area: GoalArea, val description: String, val stepsByPeriod: List<Pair<String, String>>) : GoalsEvent
    data class AddStep(val goalId: String, val periodKey: String, val title: String) : GoalsEvent
    data class SetStepStatus(val step: GoalStepEntity, val status: MilestoneStatus) : GoalsEvent
    data class SendStepToTasks(val step: GoalStepEntity, val createdByRole: String) : GoalsEvent
    data class SetGoalStatus(val goalId: String, val status: GoalStatus) : GoalsEvent
    data class DeleteGoal(val goalId: String) : GoalsEvent
}
