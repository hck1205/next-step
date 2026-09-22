package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    val events: Flow<List<EventEntity>>
    suspend fun save(event: EventEntity)
    suspend fun delete(id: String)
}
