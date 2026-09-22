package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 구성원(학부모/멘토/학생)이 서로에게 남기는 짧은 메모(격려, 요청, 피드백 등). */
@Entity(tableName = "notes", indices = [Index("familyId"), Index("createdAt")])
data class NoteEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val authorRole: String,
    val authorName: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
