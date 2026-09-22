package com.nextstep.app.ui.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.SubjectRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.data.repository.TopicRepository
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubjectDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val streams: FamilyDataStreams,
    private val subjects: SubjectRepository,
    private val topics: TopicRepository,
    private val tasks: TaskRepository,
) : ViewModel() {
    private val subjectId: String = checkNotNull(savedStateHandle.get<String>("subjectId"))

    val state: StateFlow<SubjectDetailUiState> = combine(subjects.observe(subjectId), topics.observeBySubject(subjectId), streams.subjects) { s, t, all ->
        SubjectDetailUiState(s, t, all)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubjectDetailUiState())

    fun addTopics(raw: String) = viewModelScope.launch {
        val titles = raw.split("\n", ",").map { it.trim() }.filter { it.isNotEmpty() }
        if (titles.isNotEmpty()) topics.add(subjectId, titles)
    }

    fun setStatus(topic: TopicEntity, status: TopicStatus) = viewModelScope.launch { topics.setStatus(topic.id, status) }
    fun setConfidence(topic: TopicEntity, value: Int) = viewModelScope.launch { topics.update(topic.copy(confidence = value)) }
    fun rename(topic: TopicEntity, title: String) = viewModelScope.launch { topics.update(topic.copy(title = title)) }
    fun delete(topic: TopicEntity) = viewModelScope.launch { topics.delete(topic.id) }

    /** 학급 진도를 이 단원까지로 설정. */
    fun setClassProgress(upToOrderIndex: Int) = viewModelScope.launch { topics.setClassProgress(subjectId, upToOrderIndex) }

    fun addTask(topic: TopicEntity, type: TaskType, createdByRole: String) = viewModelScope.launch {
        val subject = state.value.subject ?: return@launch
        tasks.save(
            TaskEntity(
                familyId = "", subjectId = subject.id, topicId = topic.id, title = "${subject.name} ${topic.title} ${type.label}",
                type = type, dueDate = DateUtils.today().plusDays(if (createdByRole == "STUDENT") 0 else 1).toEpochDay(), createdByRole = createdByRole,
            ),
        )
    }

    fun updateSubject(subject: SubjectEntity) = viewModelScope.launch { subjects.save(subject) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: SubjectDetailEvent) {
        when (event) {
            is SubjectDetailEvent.AddTopics -> addTopics(event.raw)
            is SubjectDetailEvent.SetStatus -> setStatus(event.topic, event.status)
            is SubjectDetailEvent.SetConfidence -> setConfidence(event.topic, event.value)
            is SubjectDetailEvent.Rename -> rename(event.topic, event.title)
            is SubjectDetailEvent.Delete -> delete(event.topic)
            is SubjectDetailEvent.SetClassProgress -> setClassProgress(event.upToOrderIndex)
            is SubjectDetailEvent.AddTask -> addTask(event.topic, event.type, event.createdByRole)
            is SubjectDetailEvent.UpdateSubject -> updateSubject(event.subject)
        }
    }

}
