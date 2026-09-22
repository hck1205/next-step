package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    val topics: Flow<List<TopicEntity>>
    fun observeBySubject(subjectId: String): Flow<List<TopicEntity>>
    suspend fun add(subjectId: String, titles: List<String>)
    suspend fun update(topic: TopicEntity)
    suspend fun setStatus(id: String, status: TopicStatus)
    /** 학급 진도: 이 순번까지(포함) 수업에서 다룬 것으로 표시. -1 이면 시작 전. */
    suspend fun setClassProgress(subjectId: String, upToOrderIndex: Int)
    suspend fun delete(id: String)
}
