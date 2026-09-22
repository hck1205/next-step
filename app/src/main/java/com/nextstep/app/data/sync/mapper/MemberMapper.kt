package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.sync.EntityMapper

object MemberMapper : EntityMapper<MemberEntity> {
    override val collection = "members"

    override fun toMap(entity: MemberEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "role" to role, "name" to name, "title" to title, "subjectIds" to subjectIds,
            "mentorEnabled" to mentorEnabled, "gradeYear" to gradeYear, "joinedAt" to joinedAt, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): MemberEntity = MemberEntity(
        id = id, familyId = data.str("familyId"), role = data.str("role"), name = data.str("name"), title = data.str("title"),
        subjectIds = data.str("subjectIds"), mentorEnabled = data.bool("mentorEnabled"), gradeYear = data.int("gradeYear"), joinedAt = data.long("joinedAt"),
        updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
