package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.RewardEntity
import com.nextstep.app.data.sync.EntityMapper

object RewardMapper : EntityMapper<RewardEntity> {
    override val collection = "rewards"

    override fun toMap(entity: RewardEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "kind" to kind, "targetId" to targetId, "title" to title, "createdByRole" to createdByRole,
            "givenAt" to givenAt, "givenByRole" to givenByRole, "createdAt" to createdAt, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): RewardEntity = RewardEntity(
        id = id, familyId = data.str("familyId"), kind = data.str("kind"), targetId = data.str("targetId"), title = data.str("title"),
        createdByRole = data.str("createdByRole"), givenAt = data.longOrNull("givenAt"), givenByRole = data.str("givenByRole"),
        createdAt = data.long("createdAt"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
