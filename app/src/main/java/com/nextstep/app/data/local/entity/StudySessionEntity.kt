package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions", indices = [Index("familyId"), Index("startAt")])
data class StudySessionEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String? = null,
    val startAt: Long,
    val endAt: Long,
    val durationMinutes: Int,
    val note: String = "",
    /** 타이머로 기록했는지, 직접 입력했는지. */
    val fromTimer: Boolean = false,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
