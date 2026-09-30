package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import kotlinx.coroutines.flow.Flow

/** 장기 목표와 구간별 단계. 단계 계산은 domain(GoalPlanner)이 하고 여기서는 저장만 합니다. */
interface GoalRepository {
    val goals: Flow<List<GoalEntity>>
    val steps: Flow<List<GoalStepEntity>>

    /** 목표와 단계를 함께 저장합니다. familyId 가 비어 있으면 현재 가족으로 채웁니다. */
    suspend fun add(goal: GoalEntity, steps: List<GoalStepEntity>)
    suspend fun addStep(step: GoalStepEntity)
    suspend fun setStepStatus(stepId: String, status: MilestoneStatus)
    suspend fun setStepTask(stepId: String, taskId: String?)
    /** 달성(DONE)이면 달성 시각을 남기고, 다시 열면 지웁니다. */
    suspend fun setGoalStatus(goalId: String, status: GoalStatus)
    /** 이 목표가 이어지는 목표를 바꿉니다(null 이면 끊기). 자기 자신·순환은 무시합니다. */
    suspend fun link(goalId: String, leadsTo: String?)
    /** 제목·이유·기한만 고칩니다. */
    suspend fun edit(goalId: String, title: String, description: String, targetDate: Long?)
    /** 목표와 단계를 모두 소프트 삭제합니다. */
    suspend fun delete(goalId: String)
}
