package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.sync.EntityMapper

object SubjectMapper : EntityMapper<SubjectEntity> {
    override val collection = "subjects"

    override fun toMap(entity: SubjectEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "name" to name, "color" to color, "teacher" to teacher,
            "weeklyGoalMinutes" to weeklyGoalMinutes, "orderIndex" to orderIndex, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): SubjectEntity = SubjectEntity(
        id = id, familyId = data.str("familyId"), name = data.str("name"), color = data.long("color", DEFAULT_COLOR),
        teacher = data.str("teacher"), weeklyGoalMinutes = data.int("weeklyGoalMinutes"), orderIndex = data.int("orderIndex"),
        updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )

    private const val DEFAULT_COLOR = 0xFF4F46E5
}
