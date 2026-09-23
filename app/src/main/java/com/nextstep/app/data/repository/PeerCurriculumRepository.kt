package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.PeerTopicEntity
import kotlinx.coroutines.flow.Flow

/** 다른 가족들의 학기별 단원 통계(읽기 전용). 비어 있으면 화면은 카탈로그만 보여 줍니다. */
interface PeerCurriculumRepository {
    val peerTopics: Flow<List<PeerTopicEntity>>
}
