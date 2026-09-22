package com.nextstep.app.ui.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.GradeEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.StudyStats
import com.nextstep.app.domain.SubjectScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class GradesUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val grades: List<GradeEntity> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val filterSubjectId: String? = null,
) {
    val filtered: List<GradeEntity> get() = if (filterSubjectId == null) grades else grades.filter { it.subjectId == filterSubjectId }
    val overallAverage: Double? get() = grades.takeIf { it.isNotEmpty() }?.map { it.percent }?.average()
}

class GradesViewModel(private val repository: StudyRepository) : ViewModel() {
    private val filter = MutableStateFlow<String?>(null)

    val state: StateFlow<GradesUiState> = combine(repository.subjects, repository.grades, filter) { subjects, grades, f ->
        GradesUiState(subjects, grades, StudyStats.subjectScores(grades, subjects), f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GradesUiState())

    fun setFilter(subjectId: String?) { filter.value = subjectId }

    fun save(existing: GradeEntity?, subjectId: String, title: String, examType: ExamType, score: Double, maxScore: Double, classAverage: Double?, date: LocalDate, memo: String) = viewModelScope.launch {
        val g = (existing ?: GradeEntity(familyId = "", subjectId = subjectId, title = title, score = score, date = date.toEpochDay())).copy(
            subjectId = subjectId, title = title, examType = examType, score = score, maxScore = maxScore, classAverage = classAverage, date = date.toEpochDay(), memo = memo,
        )
        repository.saveGrade(g)
    }

    fun delete(id: String) = viewModelScope.launch { repository.deleteGrade(id) }
}
