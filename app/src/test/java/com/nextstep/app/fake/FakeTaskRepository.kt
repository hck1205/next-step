package com.nextstep.app.fake

import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTaskRepository : TaskRepository {
    override val tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val saved = mutableListOf<TaskEntity>()

    override suspend fun save(task: TaskEntity) {
        saved += task
        tasks.value = tasks.value.filter { it.id != task.id } + task
    }

    override suspend fun setDone(id: String, done: Boolean) {
        tasks.value.firstOrNull { it.id == id }?.let { save(it.copy(done = done)) }
    }

    override suspend fun delete(id: String) {
        tasks.value.firstOrNull { it.id == id }?.let { save(it.copy(deleted = true)) }
    }
}
