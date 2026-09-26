package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.sync.EntityMapper

object WeekPlanMapper : EntityMapper<WeekPlanEntity> {
    override val collection = "weekPlans"

    override fun toMap(entity: WeekPlanEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "weekStart" to weekStart, "goals" to goals, "doneMask" to doneMask, "plannedMinutes" to plannedMinutes,
            "authorRole" to authorRole, "approvedAt" to approvedAt, "mood" to mood, "good" to good, "hard" to hard, "change" to change,
            "reflectedByRole" to reflectedByRole, "reflectedAt" to reflectedAt, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): WeekPlanEntity = WeekPlanEntity(
        id = id, familyId = data.str("familyId"), weekStart = data.long("weekStart"), goals = data.str("goals"), doneMask = data.int("doneMask"),
        plannedMinutes = data.int("plannedMinutes"), authorRole = data.str("authorRole"), approvedAt = data.longOrNull("approvedAt"), mood = data.int("mood"),
        good = data.str("good"), hard = data.str("hard"), change = data.str("change"), reflectedByRole = data.str("reflectedByRole"),
        reflectedAt = data.longOrNull("reflectedAt"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
