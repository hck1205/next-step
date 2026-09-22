package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.sync.EntityMapper

object GoalStepMapper : EntityMapper<GoalStepEntity> {
    override val collection = "goalSteps"

    override fun toMap(entity: GoalStepEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "goalId" to goalId, "periodKey" to periodKey, "orderIndex" to orderIndex, "title" to title,
            "detail" to detail, "status" to status.name, "taskId" to taskId, "doneAt" to doneAt, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): GoalStepEntity = GoalStepEntity(
        id = id, familyId = data.str("familyId"), goalId = data.str("goalId"), periodKey = data.str("periodKey"), orderIndex = data.int("orderIndex"),
        title = data.str("title"), detail = data.str("detail"), status = MilestoneStatus.from(data.strOrNull("status")), taskId = data.strOrNull("taskId"),
        doneAt = data.longOrNull("doneAt"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
