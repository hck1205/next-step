package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.sync.EntityMapper

object TaskMapper : EntityMapper<TaskEntity> {
    override val collection = "tasks"

    override fun toMap(entity: TaskEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "subjectId" to subjectId, "topicId" to topicId, "title" to title, "type" to type.name,
            "dueDate" to dueDate, "done" to done, "createdByRole" to createdByRole, "note" to note, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): TaskEntity = TaskEntity(
        id = id, familyId = data.str("familyId"), subjectId = data.strOrNull("subjectId"), topicId = data.strOrNull("topicId"),
        title = data.str("title"), type = TaskType.from(data.strOrNull("type")), dueDate = data.long("dueDate"), done = data.bool("done"),
        createdByRole = data.str("createdByRole"), note = data.str("note"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
