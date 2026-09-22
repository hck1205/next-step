package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.ExamType

@Entity(tableName = "grades", indices = [Index("familyId"), Index("subjectId")])
data class GradeEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String,
    val title: String,
    val examType: ExamType = ExamType.QUIZ,
    val score: Double,
    val maxScore: Double = 100.0,
    /** 반 평균. 입력하지 않으면 null. */
    val classAverage: Double? = null,
    /** 시험일 (epoch day). */
    val date: Long,
    val memo: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    val percent: Double get() = if (maxScore > 0) score / maxScore * 100.0 else 0.0
}
