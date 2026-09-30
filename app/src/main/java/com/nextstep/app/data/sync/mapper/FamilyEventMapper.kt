package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.data.sync.EntityMapper

object FamilyEventMapper : EntityMapper<FamilyEventEntity> {
    override val collection = "familyEvents"

    override fun toMap(entity: FamilyEventEntity): Map<String, Any?> = with(entity) {
        syncFields() + mapOf(
            "title" to title, "kind" to kind, "startDate" to startDate, "endDate" to endDate,
            "allDay" to allDay, "startMinute" to startMinute, "endMinute" to endMinute, "memberIds" to memberIds, "keeperId" to keeperId,
            "repeat" to repeat, "repeatUntil" to repeatUntil, "headsUp" to headsUp, "location" to location, "bring" to bring, "memo" to memo,
            "createdById" to createdById, "createdByRole" to createdByRole, "createdAt" to createdAt,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): FamilyEventEntity = FamilyEventEntity(
        id = id, familyId = data.str("familyId"), title = data.str("title"), kind = data.str("kind"),
        startDate = data.long("startDate"), endDate = data.long("endDate", data.long("startDate")), allDay = data.bool("allDay"),
        startMinute = data.int("startMinute"), endMinute = data.int("endMinute"), memberIds = data.str("memberIds"), keeperId = data.str("keeperId"),
        repeat = data.str("repeat"), repeatUntil = data.longOrNull("repeatUntil"), headsUp = data.str("headsUp"), location = data.str("location"),
        bring = data.str("bring"), memo = data.str("memo"), createdById = data.str("createdById"), createdByRole = data.str("createdByRole"),
        createdAt = data.long("createdAt"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
