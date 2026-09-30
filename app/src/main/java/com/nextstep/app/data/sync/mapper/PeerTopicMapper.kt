package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.PeerTopicEntity
import com.nextstep.app.data.sync.EntityMapper

/** 공용 읽기 전용 컬렉션. 서버 집계 작업이 쓰고 앱은 받기만 합니다. */
object PeerTopicMapper : EntityMapper<PeerTopicEntity> {
    override val collection = "peerTopics"

    override fun toMap(entity: PeerTopicEntity): Map<String, Any?> = with(entity) {
        mapOf("id" to id, "periodKey" to periodKey, "subject" to subject, "title" to title, "families" to families, "coveredRatio" to coveredRatio, "updatedAt" to updatedAt, "deleted" to deleted)
    }

    override fun fromMap(id: String, data: Map<String, Any?>): PeerTopicEntity = PeerTopicEntity(
        id = id, periodKey = data.str("periodKey"), subject = data.str("subject"), title = data.str("title"), families = data.int("families"),
        coveredRatio = data.dbl("coveredRatio"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
