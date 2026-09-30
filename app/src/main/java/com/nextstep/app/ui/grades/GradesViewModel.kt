package com.nextstep.app.ui.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GradeRepository
import com.nextstep.app.domain.entry.GradeDraft
import com.nextstep.app.domain.stats.ScoreStats
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class GradesViewModel(
    private val streams: FamilyDataStreams,
    private val grades: GradeRepository,
) : ViewModel() {
    private val filter = MutableStateFlow<String?>(null)

    val state: StateFlow<GradesUiState> = combine(streams.subjects, streams.grades, filter) { subjects, grades, f ->
        GradesUiState(
            subjects, grades, StudyStats.subjectScores(grades, subjects), f,
            filtered = if (f == null) grades else grades.filter { it.subjectId == f }, overallAverage = ScoreStats.averagePercent(grades),
        )
    }.asUiState(viewModelScope, GradesUiState())

    fun setFilter(subjectId: String?) { filter.value = subjectId }

    fun save(existing: GradeEntity?, draft: GradeDraft) {
        viewModelScope.launch { grades.save(draft.toEntity(existing)) }
    }

    fun delete(id: String) { viewModelScope.launch { grades.delete(id) } }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: GradesEvent) {
        when (event) {
            is GradesEvent.SetFilter -> setFilter(event.subjectId)
            is GradesEvent.Save -> save(event.existing, event.draft)
            is GradesEvent.Delete -> delete(event.id)
        }
    }

}
