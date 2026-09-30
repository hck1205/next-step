package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.model.RoadmapStatus
import kotlinx.coroutines.flow.Flow

interface RoadmapRepository {
    val roadmap: Flow<List<RoadmapItemEntity>>
    suspend fun save(item: RoadmapItemEntity)
    suspend fun setStatus(id: String, status: RoadmapStatus)
    suspend fun delete(id: String)
}
