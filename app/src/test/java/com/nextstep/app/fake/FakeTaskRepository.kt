package com.nextstep.app.fake

import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** [streams] 를 주면 쓰기가 그 파사드의 tasks 에도 보여, 실제 앱처럼 저장소 쓰기가 스트림 읽기에 반영됩니다. */
class FakeTaskRepository(streams: FakeFamilyDataStreams? = null) : TaskRepository {
    override val tasks: MutableStateFlow<List<TaskEntity>> = streams?.tasks ?: MutableStateFlow(emptyList())
    val saved = mutableListOf<TaskEntity>()

    override suspend fun save(task: TaskEntity) {
        saved += task
        tasks.value = tasks.value.filter { it.id != task.id } + task
    }

    override suspend fun setDone(id: String, done: Boolean) {
        tasks.value.firstOrNull { it.id == id }?.let { save(it.copy(done = done, doneAt = if (done) DONE_AT else null)) }
    }

    override suspend fun delete(id: String) {
        tasks.value.firstOrNull { it.id == id }?.let { save(it.copy(deleted = true)) }
    }

    companion object {
        /** 가짜 저장소가 남기는 끝낸 시각. */
        const val DONE_AT = 1L
    }
}
