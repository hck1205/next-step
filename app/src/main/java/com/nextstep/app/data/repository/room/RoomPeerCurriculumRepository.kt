package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.PeerTopicDao
import com.nextstep.app.data.local.entity.PeerTopicEntity
import com.nextstep.app.data.repository.PeerCurriculumRepository
import kotlinx.coroutines.flow.Flow

class RoomPeerCurriculumRepository(dao: PeerTopicDao) : PeerCurriculumRepository {
    override val peerTopics: Flow<List<PeerTopicEntity>> = dao.observeAll()
}
