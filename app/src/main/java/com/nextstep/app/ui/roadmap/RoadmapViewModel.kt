package com.nextstep.app.ui.roadmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.RoadmapItemEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.StudyStats
import com.nextstep.app.domain.SubjectProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class RoadmapUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val items: List<RoadmapItemEntity> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
    val topics: List<TopicEntity> = emptyList(),
    val studentName: String = "",
) {
    val active: List<RoadmapItemEntity> get() = items.filter { it.status != RoadmapStatus.DONE }
    val done: List<RoadmapItemEntity> get() = items.filter { it.status == RoadmapStatus.DONE }
    val completion: Float get() = if (items.isEmpty()) 0f else done.size.toFloat() / items.size

    /** 멘토가 로드맵을 짤 때 참고할 추천: 복습 밀린 단원, 다음 예습 단원. */
    val suggestions: List<Pair<SubjectEntity, String>> get() = progress.flatMap { p ->
        p.reviewQueue.take(1).map { p.subject to "복습 보강: ${it.title}" } + p.previewQueue.take(1).map { p.subject to "선행 예습: ${it.title}" }
    }
}

class RoadmapViewModel(private val repository: StudyRepository) : ViewModel() {
    val state: StateFlow<RoadmapUiState> = combine(repository.subjects, repository.roadmap, repository.topics, repository.profile) { subjects, items, topics, profile ->
        RoadmapUiState(subjects, items, StudyStats.subjectProgress(topics, subjects), topics, profile.studentName)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoadmapUiState())

    fun save(existing: RoadmapItemEntity?, subjectId: String?, title: String, description: String, resource: String, targetDate: LocalDate?) = viewModelScope.launch {
        val base = existing ?: RoadmapItemEntity(familyId = "", title = title, orderIndex = state.value.items.size)
        repository.saveRoadmapItem(base.copy(subjectId = subjectId, title = title, description = description, resource = resource, targetDate = targetDate?.toEpochDay()))
    }

    fun setStatus(id: String, status: RoadmapStatus) = viewModelScope.launch { repository.setRoadmapStatus(id, status) }
    fun delete(id: String) = viewModelScope.launch { repository.deleteRoadmapItem(id) }

    /** 추천 항목을 로드맵에 바로 추가. */
    fun addSuggestion(subject: SubjectEntity, title: String) = save(null, subject.id, title, "", "", LocalDate.now().plusDays(7))
}
