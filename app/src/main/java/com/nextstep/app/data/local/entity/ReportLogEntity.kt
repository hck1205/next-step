package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 멘토가 학부모에게 보낸 수업 리포트 한 번(보낸 기록). 멘토는 자기가 보낸 것만 봅니다(MentorScopedStreams). */
@Entity(tableName = "report_logs", indices = [Index("familyId")])
data class ReportLogEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    /** ReportKind 이름(WEEK · MONTH). */
    val kind: String,
    val title: String,
    val sentById: String = "",
    val sentByName: String = "",
    val sentAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
