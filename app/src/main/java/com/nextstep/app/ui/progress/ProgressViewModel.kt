package com.nextstep.app.ui.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.domain.StudyStats
import com.nextstep.app.domain.SubjectProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProgressUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
)

class ProgressViewModel(private val repository: StudyRepository) : ViewModel() {
    val state: StateFlow<ProgressUiState> = combine(repository.subjects, repository.topics) { subjects, topics ->
        ProgressUiState(subjects, StudyStats.subjectProgress(topics, subjects, queueSize = 2))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun addSubject(name: String, color: Long, goalMinutes: Int, teacher: String) = viewModelScope.launch {
        repository.saveSubject(
            SubjectEntity(familyId = "", name = name, color = color, weeklyGoalMinutes = goalMinutes, teacher = teacher, orderIndex = state.value.subjects.size),
        )
    }

    fun updateSubject(subject: SubjectEntity) = viewModelScope.launch { repository.saveSubject(subject) }
    fun deleteSubject(id: String) = viewModelScope.launch { repository.deleteSubject(id) }
}

data class SubjectDetailUiState(
    val subject: SubjectEntity? = null,
    val topics: List<TopicEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
) {
    val classIndex: Int get() = topics.filter { it.classCovered }.maxOfOrNull { it.orderIndex } ?: -1
    val previewQueue: List<TopicEntity> get() = topics.filter { !it.classCovered && it.status.order < TopicStatus.PREVIEWED.order }
    val reviewQueue: List<TopicEntity> get() = topics.filter { it.classCovered && it.status.order < TopicStatus.REVIEWED.order }
}

class SubjectDetailViewModel(savedStateHandle: SavedStateHandle, private val repository: StudyRepository) : ViewModel() {
    private val subjectId: String = checkNotNull(savedStateHandle["subjectId"])

    val state: StateFlow<SubjectDetailUiState> = combine(repository.observeSubject(subjectId), repository.observeTopics(subjectId), repository.subjects) { s, t, all ->
        SubjectDetailUiState(s, t, all)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubjectDetailUiState())

    fun addTopics(raw: String) = viewModelScope.launch {
        val titles = raw.split("\n", ",").map { it.trim() }.filter { it.isNotEmpty() }
        if (titles.isNotEmpty()) repository.addTopics(subjectId, titles)
    }

    fun setStatus(topic: TopicEntity, status: TopicStatus) = viewModelScope.launch { repository.setTopicStatus(topic.id, status) }
    fun setConfidence(topic: TopicEntity, value: Int) = viewModelScope.launch { repository.updateTopic(topic.copy(confidence = value)) }
    fun rename(topic: TopicEntity, title: String) = viewModelScope.launch { repository.updateTopic(topic.copy(title = title)) }
    fun delete(topic: TopicEntity) = viewModelScope.launch { repository.deleteTopic(topic.id) }

    /** 학급 진도를 이 단원까지로 설정. */
    fun setClassProgress(upToOrderIndex: Int) = viewModelScope.launch { repository.setClassProgress(subjectId, upToOrderIndex) }

    fun addTask(topic: TopicEntity, type: TaskType) = viewModelScope.launch {
        val subject = state.value.subject ?: return@launch
        repository.saveTask(
            TaskEntity(
                familyId = "", subjectId = subject.id, topicId = topic.id, title = "${subject.name} ${topic.title} ${type.label}",
                type = type, dueDate = DateUtils.today().toEpochDay(), createdByRole = "STUDENT",
            ),
        )
    }

    fun updateSubject(subject: SubjectEntity) = viewModelScope.launch { repository.saveSubject(subject) }
}
