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
    /** 사용자가 만든 목표(목표 트리)의 세부 할 일이면 그 목표 id. */
    val goalId: String? = null,
    /** 끝낸 시각. 기록(히스토리)과 달성률의 기준이며, 다시 열면 지웁니다. */
    val doneAt: Long? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    /** 학생이 스스로 만든 할 일인지. 자기주도 비율의 단위. */
    val isStudentMade: Boolean get() = createdByRole == Role.STUDENT.name
}
