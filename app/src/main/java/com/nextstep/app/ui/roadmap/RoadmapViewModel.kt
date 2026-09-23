package com.nextstep.app.ui.roadmap

import com.nextstep.app.domain.time.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.RoadmapRepository
import com.nextstep.app.domain.stats.StudyStats
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.ui.common.asUiState

class RoadmapViewModel(
    private val streams: FamilyDataStreams,
    private val roadmap: RoadmapRepository,
) : ViewModel() {
    val state: StateFlow<RoadmapUiState> = combine(streams.subjects, streams.roadmap, streams.topics, streams.profile, streams.contents) { subjects, items, topics, profile, contents ->
        RoadmapUiState(subjects, items, StudyStats.subjectProgress(topics, subjects), topics, profile.studentName, contents)
    }.asUiState(viewModelScope, RoadmapUiState())

    fun save(existing: RoadmapItemEntity?, subjectId: String?, title: String, description: String, resource: String, targetDate: LocalDate?, contentId: String?) = viewModelScope.launch {
        val base = existing ?: RoadmapItemEntity(familyId = "", title = title, orderIndex = state.value.items.size)
        roadmap.save(base.copy(subjectId = subjectId, title = title, description = description, resource = resource, targetDate = targetDate?.toEpochDay(), contentId = contentId))
    }

    fun setStatus(id: String, status: RoadmapStatus) = viewModelScope.launch { roadmap.setStatus(id, status) }
    fun delete(id: String) = viewModelScope.launch { roadmap.delete(id) }

    /** 추천 항목을 로드맵에 바로 추가. */
    fun addSuggestion(subject: SubjectEntity, title: String) = save(null, subject.id, title, "", "", DateUtils.today().plusDays(7), null)

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: RoadmapEvent) {
        when (event) {
            is RoadmapEvent.Save -> save(event.existing, event.subjectId, event.title, event.description, event.resource, event.targetDate, event.contentId)
            is RoadmapEvent.SetStatus -> setStatus(event.id, event.status)
            is RoadmapEvent.Delete -> delete(event.id)
            is RoadmapEvent.AddSuggestion -> addSuggestion(event.subject, event.title)
        }
    }

}
