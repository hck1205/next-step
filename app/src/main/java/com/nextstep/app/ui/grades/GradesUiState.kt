package com.nextstep.app.ui.grades

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.stats.SubjectScore

data class GradesUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val grades: List<GradeEntity> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val filterSubjectId: String? = null,
) {
    val filtered: List<GradeEntity> get() = if (filterSubjectId == null) grades else grades.filter { it.subjectId == filterSubjectId }
    val overallAverage: Double? get() = grades.takeIf { it.isNotEmpty() }?.map { it.percent }?.average()
}
