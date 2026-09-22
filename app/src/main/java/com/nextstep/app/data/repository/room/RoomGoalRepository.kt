package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.GoalDao
import com.nextstep.app.data.local.dao.GoalStepDao
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomGoalRepository(
    private val goalDao: GoalDao,
    private val stepDao: GoalStepDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), GoalRepository {

    override val goals: Flow<List<GoalEntity>> = scope.scopedList { goalDao.observeAll(it) }
    override val steps: Flow<List<GoalStepEntity>> = scope.scopedList { stepDao.observeAll(it) }

    override suspend fun add(goal: GoalEntity, steps: List<GoalStepEntity>) {
        if (goal.title.isBlank()) return
        val familyId = familyIdOr(goal.familyId)
        val now = now()
        goalDao.upsert(goal.copy(familyId = familyId, title = goal.title.trim(), createdByRole = scope.currentProfile().role?.name ?: "", updatedAt = now, dirty = true))
        stepDao.upsertAll(steps.map { it.copy(familyId = familyId, goalId = goal.id, updatedAt = now, dirty = true) })
        pushLater()
    }

    override suspend fun addStep(step: GoalStepEntity) {
        if (step.title.isBlank()) return
        stepDao.upsert(step.copy(familyId = familyIdOr(step.familyId), title = step.title.trim(), updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun setStepStatus(stepId: String, status: MilestoneStatus) = modifyStep(stepId) {
        it.copy(status = status, doneAt = if (status == MilestoneStatus.DONE) now() else null)
    }

    override suspend fun setStepTask(stepId: String, taskId: String?) = modifyStep(stepId) { it.copy(taskId = taskId) }

    override suspend fun setGoalStatus(goalId: String, status: GoalStatus) {
        val goal = goalDao.getById(goalId) ?: return
        goalDao.upsert(goal.copy(status = status, updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun delete(goalId: String) {
        val goal = goalDao.getById(goalId) ?: return
        val now = now()
        goalDao.upsert(goal.copy(deleted = true, updatedAt = now, dirty = true))
        stepDao.upsertAll(stepDao.getByGoal(goalId).map { it.copy(deleted = true, updatedAt = now, dirty = true) })
        pushLater()
    }

    private suspend fun modifyStep(id: String, change: (GoalStepEntity) -> GoalStepEntity) {
        val step = stepDao.getById(id) ?: return
        stepDao.upsert(change(step).copy(updatedAt = now(), dirty = true))
        pushLater()
    }
}
