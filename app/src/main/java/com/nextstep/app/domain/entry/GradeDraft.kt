package com.nextstep.app.domain.entry

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.model.ExamType
import java.time.LocalDate

/**
 * 성적 입력창에서 적은 값 한 벌. 성적 탭과 기록하기가 같은 모양으로 저장하도록 성적으로 바꾸는 법을 여기 한 곳에 둡니다.
 * 가족 id 는 저장소가 채웁니다.
 */
data class GradeDraft(
    val subjectId: String,
    val title: String,
    val examType: ExamType,
    val score: Double,
    val maxScore: Double,
    val classAverage: Double?,
    val date: LocalDate,
    val memo: String,
) {
    /** 새 성적이거나, [existing] 을 이 값으로 고친 성적(id 유지). */
    fun toEntity(existing: GradeEntity? = null): GradeEntity {
        val base = existing ?: GradeEntity(familyId = "", subjectId = subjectId, title = title.trim(), score = score, date = date.toEpochDay())
        return base.copy(
            subjectId = subjectId, title = title.trim(), examType = examType, score = score, maxScore = maxScore,
            classAverage = classAverage, date = date.toEpochDay(), memo = memo.trim(),
        )
    }
}
