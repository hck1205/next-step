package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.EventType

@Entity(tableName = "events", indices = [Index("familyId"), Index("startAt")])
data class EventEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String? = null,
    val title: String,
    val type: EventType = EventType.OTHER,
    /** 시작 시각 (epoch millis). 반복 일정이면 첫 발생일 기준. */
    val startAt: Long,
    val endAt: Long,
    /** true 면 매주 같은 요일/시간에 반복 (시간표). */
    val repeatWeekly: Boolean = false,
    val location: String = "",
    val memo: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
