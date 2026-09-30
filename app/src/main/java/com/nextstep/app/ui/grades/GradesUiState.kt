package com.nextstep.app.ui.grades

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.stats.SubjectScore

/** 성적 화면. 과목 필터 결과와 전체 평균은 ViewModel 이 한 번 계산합니다. */
data class GradesUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val grades: List<GradeEntity> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val filterSubjectId: String? = null,
    val filtered: List<GradeEntity> = emptyList(),
    val overallAverage: Double? = null,
)
