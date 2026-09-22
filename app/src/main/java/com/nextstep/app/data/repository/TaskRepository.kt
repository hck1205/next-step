package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    val tasks: Flow<List<TaskEntity>>
    suspend fun save(task: TaskEntity)
    suspend fun setDone(id: String, done: Boolean)
    suspend fun delete(id: String)
}
