package com.nextstep.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.prefs.RunningTimer
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.domain.EventOccurrence
import com.nextstep.app.domain.StudyStats
import com.nextstep.app.domain.SubjectProgress
import com.nextstep.app.domain.UpcomingExam
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val displayName: String = "",
    val subjects: List<SubjectEntity> = emptyList(),
    val todayEvents: List<EventOccurrence> = emptyList(),
    val pendingTasks: List<TaskEntity> = emptyList(),
    val todayMinutes: Int = 0,
    val weekMinutes: Int = 0,
    val weekGoalMinutes: Int = 0,
    val runningTimer: RunningTimer? = null,
    val progress: List<SubjectProgress> = emptyList(),
    val nextExam: UpcomingExam? = null,
    val loaded: Boolean = false,
) {
    val previewQueue: List<Pair<SubjectEntity, TopicEntity>> get() = progress.flatMap { p -> p.previewQueue.take(1).map { p.subject to it } }
    val reviewQueue: List<Pair<SubjectEntity, TopicEntity>> get() = progress.flatMap { p -> p.reviewQueue.take(2).map { p.subject to it } }
}

class HomeViewModel(private val repository: StudyRepository) : ViewModel() {

    private val base = combine(repository.profile, repository.subjects, repository.events, repository.tasks, repository.sessions) { profile, subjects, events, tasks, sessions ->
        HomeUiState(
            displayName = profile.displayName,
            subjects = subjects,
            todayEvents = StudyStats.eventsOn(DateUtils.today(), events),
            pendingTasks = StudyStats.pendingTasks(tasks),
            todayMinutes = StudyStats.todayMinutes(sessions),
            weekMinutes = StudyStats.weekMinutes(sessions),
            weekGoalMinutes = subjects.sumOf { it.weeklyGoalMinutes },
            nextExam = StudyStats.upcomingExams(events, tasks).firstOrNull(),
            loaded = true,
        )
    }

    val state: StateFlow<HomeUiState> = combine(base, repository.topics, repository.runningTimer) { s, topics, timer ->
        s.copy(progress = StudyStats.subjectProgress(topics, s.subjects), runningTimer = timer)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleTask(task: TaskEntity) = viewModelScope.launch { repository.setTaskDone(task.id, !task.done) }

    fun markTopic(topic: TopicEntity, status: TopicStatus) = viewModelScope.launch { repository.setTopicStatus(topic.id, status) }

    fun addQuickTask(subject: SubjectEntity, topic: TopicEntity, type: TaskType) = viewModelScope.launch {
        repository.saveTask(
            TaskEntity(
                familyId = "", subjectId = subject.id, topicId = topic.id,
                title = "${subject.name} ${topic.title} ${type.label}", type = type,
                dueDate = DateUtils.today().toEpochDay(), createdByRole = "STUDENT",
            ),
        )
    }
}
