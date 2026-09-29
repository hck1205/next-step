package com.nextstep.app.ui.common

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.journey.JourneyPeriod
import com.nextstep.app.domain.task.TaskDrafts
import java.time.LocalDate

/**
 * 목표 단계를 할 일로 보내고 그 단계에 할 일을 잇습니다(목표 화면·여정 공통). 이미 보낸 단계는 다시 만들지 않습니다.
 * 마감·종류 규칙은 TaskDrafts.forGoalStep, 작성자 역할은 [actingRoleName].
 */
suspend fun FamilyDataStreams.saveStepAsTask(
    step: GoalStepEntity, goal: GoalEntity?, periods: List<JourneyPeriod>, today: LocalDate, tasks: TaskRepository, goals: GoalRepository,
) {
    if (step.taskId != null) return
    val task = TaskDrafts.forGoalStep(step, goal, periods.firstOrNull { it.key == step.periodKey }, today, actingRoleName())
    tasks.save(task)
    goals.setStepTask(step.id, task.id)
}
