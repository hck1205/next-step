package com.nextstep.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.SubjectRepository
import com.nextstep.app.domain.stats.StudyStats
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.ui.common.asUiState

class ProgressViewModel(
    private val streams: FamilyDataStreams,
    private val subjects: SubjectRepository,
) : ViewModel() {
    val state: StateFlow<ProgressUiState> = combine(streams.subjects, streams.topics) { subjects, topics ->
        ProgressUiState(subjects, StudyStats.subjectProgress(topics, subjects, queueSize = 2))
    }.asUiState(viewModelScope, ProgressUiState())

    fun addSubject(name: String, color: Long, goalMinutes: Int, teacher: String) = viewModelScope.launch {
        subjects.save(
            SubjectEntity(familyId = "", name = name, color = color, weeklyGoalMinutes = goalMinutes, teacher = teacher, orderIndex = state.value.subjects.size),
        )
    }

    fun updateSubject(subject: SubjectEntity) = viewModelScope.launch { subjects.save(subject) }
    fun deleteSubject(id: String) = viewModelScope.launch { subjects.delete(id) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: ProgressEvent) {
        when (event) {
            is ProgressEvent.AddSubject -> addSubject(event.name, event.color, event.goalMinutes, event.teacher)
            is ProgressEvent.UpdateSubject -> updateSubject(event.subject)
            is ProgressEvent.DeleteSubject -> deleteSubject(event.id)
        }
    }

}
