package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.EventDao
import com.nextstep.app.data.local.dao.TaskDao
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.StudyPlanRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.sync.SyncManager
import com.nextstep.app.domain.planner.StudyPlan

class RoomStudyPlanRepository(
    private val eventDao: EventDao,
    private val taskDao: TaskDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), StudyPlanRepository {

    override suspend fun apply(plan: StudyPlan) {
        if (plan.isEmpty) return
        val familyId = scope.requireFamilyId()
        val ts = now()
        eventDao.upsertAll(plan.events.map { it.copy(familyId = familyId, updatedAt = ts, dirty = true) })
        taskDao.upsertAll(plan.tasks.map { it.copy(familyId = familyId, updatedAt = ts, dirty = true) })
        pushLater()
    }
}
