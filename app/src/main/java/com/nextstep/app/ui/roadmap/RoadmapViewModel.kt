package com.nextstep.app.ui.roadmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.RoadmapTemplateEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.RoadmapRepository
import com.nextstep.app.data.repository.RoadmapTemplateRepository
import com.nextstep.app.domain.roadmap.RoadmapTemplates
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.actingRoleName
import com.nextstep.app.ui.common.asUiState
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RoadmapViewModel(
    private val streams: FamilyDataStreams,
    private val roadmap: RoadmapRepository,
    private val templates: RoadmapTemplateRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    val state: StateFlow<RoadmapUiState> = combine(streams.subjects, streams.roadmap, streams.topics, streams.profile, streams.contents) { subjects, items, topics, profile, contents ->
        RoadmapUiState.derive(subjects, items, StudyStats.subjectProgress(topics, subjects), topics, profile.studentName, contents)
    }.combine(templates.templates) { s, t -> s.copy(templates = t) }.asUiState(viewModelScope, RoadmapUiState())

    fun save(existing: RoadmapItemEntity?, subjectId: String?, title: String, description: String, resource: String, targetDate: LocalDate?, contentId: String?) {
        viewModelScope.launch {
            val base = existing ?: RoadmapItemEntity(familyId = "", title = title, orderIndex = state.value.items.size)
            roadmap.save(base.copy(subjectId = subjectId, title = title, description = description, resource = resource, targetDate = targetDate?.toEpochDay(), contentId = contentId))
        }
    }

    fun setStatus(id: String, status: RoadmapStatus) { viewModelScope.launch { roadmap.setStatus(id, status) } }
    fun delete(id: String) { viewModelScope.launch { roadmap.delete(id) } }

    /** 추천 항목을 로드맵에 바로 추가. */
    fun addSuggestion(subject: SubjectEntity, title: String) = save(null, subject.id, title, "", "", DateUtils.today().plusDays(7), null)

    /** 지금 로드맵(순서·간격)을 템플릿으로. 주 과목은 가장 많은 항목의 과목. */
    private fun saveTemplate(name: String) {
        viewModelScope.launch {
            val s = state.value
            val main = s.items.groupingBy { it.subjectId }.eachCount().maxByOrNull { it.value }?.key
            templates.save(name, s.subjects.firstOrNull { it.id == main }?.name.orEmpty(), RoadmapTemplates.fromItems(s.items))
        }
    }

    /** 템플릿을 오늘부터 다시 놓아 이 학생의 로드맵 뒤에 붙입니다. 과목은 같은 이름의 과목(없으면 과목 없이). */
    private fun applyTemplate(template: RoadmapTemplateEntity) {
        viewModelScope.launch {
            val s = state.value
            val subjectId = s.subjects.firstOrNull { it.name == template.subjectName }?.id
            RoadmapTemplates.toItems(RoadmapTemplates.decode(template.items), today(), subjectId, byName = "", byRole = streams.actingRoleName())
                .forEach { roadmap.save(it.copy(orderIndex = s.items.size + it.orderIndex)) }
        }
    }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: RoadmapEvent) {
        when (event) {
            is RoadmapEvent.Save -> save(event.existing, event.subjectId, event.title, event.description, event.resource, event.targetDate, event.contentId)
            is RoadmapEvent.SetStatus -> setStatus(event.id, event.status)
            is RoadmapEvent.Delete -> delete(event.id)
            is RoadmapEvent.AddSuggestion -> addSuggestion(event.subject, event.title)
            is RoadmapEvent.SaveTemplate -> saveTemplate(event.name)
            is RoadmapEvent.ApplyTemplate -> applyTemplate(event.template)
            is RoadmapEvent.DeleteTemplate -> viewModelScope.launch { templates.delete(event.id) }
        }
    }

}
