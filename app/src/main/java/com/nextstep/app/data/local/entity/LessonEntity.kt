package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 수업 한 번의 출결 기록(멘토가 적음, 학부모도 봄). 정해진 수업은 멘토의 수업 일정(LessonPlan)에서 계산하고, 적은 날만 행이 됩니다.
 * 멘토 한 명의 하루에 한 행([mentorId] + [date]).
 */
@Entity(tableName = "lessons", indices = [Index("familyId"), Index("date")])
data class LessonEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val mentorId: String,
    val date: Long,
    /** LessonStatus 이름. */
    val status: String,
    val note: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
