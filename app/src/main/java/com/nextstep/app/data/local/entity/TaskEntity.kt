package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType

@Entity(tableName = "tasks", indices = [Index("familyId"), Index("dueDate")])
data class TaskEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String? = null,
    val topicId: String? = null,
    val title: String,
    val type: TaskType = TaskType.OTHER,
    /** 마감일 (epoch day). */
    val dueDate: Long,
    val done: Boolean = false,
    val createdByRole: String,
    val note: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    /** 학생이 스스로 만든 할 일인지. 자기주도 비율의 단위. */
    val isStudentMade: Boolean get() = createdByRole == Role.STUDENT.name
}
