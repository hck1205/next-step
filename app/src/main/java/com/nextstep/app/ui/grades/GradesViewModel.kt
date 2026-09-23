package com.nextstep.app.ui.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GradeRepository
import com.nextstep.app.domain.stats.StudyStats
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.ui.common.asUiState

class GradesViewModel(
    private val streams: FamilyDataStreams,
    private val grades: GradeRepository,
) : ViewModel() {
    private val filter = MutableStateFlow<String?>(null)

    val state: StateFlow<GradesUiState> = combine(streams.subjects, streams.grades, filter) { subjects, grades, f ->
        GradesUiState(subjects, grades, StudyStats.subjectScores(grades, subjects), f)
    }.asUiState(viewModelScope, GradesUiState())

    fun setFilter(subjectId: String?) { filter.value = subjectId }

    fun save(existing: GradeEntity?, subjectId: String, title: String, examType: ExamType, score: Double, maxScore: Double, classAverage: Double?, date: LocalDate, memo: String) = viewModelScope.launch {
        val g = (existing ?: GradeEntity(familyId = "", subjectId = subjectId, title = title, score = score, date = date.toEpochDay())).copy(
            subjectId = subjectId, title = title, examType = examType, score = score, maxScore = maxScore, classAverage = classAverage, date = date.toEpochDay(), memo = memo,
        )
        grades.save(g)
    }

    fun delete(id: String) = viewModelScope.launch { grades.delete(id) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: GradesEvent) {
        when (event) {
            is GradesEvent.SetFilter -> setFilter(event.subjectId)
            is GradesEvent.Save -> save(event.existing, event.subjectId, event.title, event.examType, event.score, event.maxScore, event.classAverage, event.date, event.memo)
            is GradesEvent.Delete -> delete(event.id)
        }
    }

}
