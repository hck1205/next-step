package com.nextstep.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.RoadmapItemEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.prefs.RunningTimer
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.domain.EventOccurrence
import com.nextstep.app.domain.PlanOptions
import com.nextstep.app.domain.StudyPlan
import com.nextstep.app.domain.StudyPlanner
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
    val roadmap: List<RoadmapItemEntity> = emptyList(),
    val events: List<com.nextstep.app.data.local.EventEntity> = emptyList(),
    val lastPlan: StudyPlan? = null,
) {
    /** 진행 중이거나 목표일이 가까운 로드맵 항목. */
    val roadmapFocus: List<RoadmapItemEntity> get() = roadmap.filter { it.status != RoadmapStatus.DONE }
        .sortedWith(compareBy<RoadmapItemEntity> { it.status != RoadmapStatus.IN_PROGRESS }.thenBy { it.targetDate ?: Long.MAX_VALUE }).take(3)
    /** 지금 배우는 과목: 학급 진도가 시작됐고 아직 끝나지 않은 과목. */
    val activeSubjects: List<SubjectProgress> get() = progress.filter { it.classCovered > 0 && it.classCovered < it.total }
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
            events = events,
            loaded = true,
        )
    }

    private val lastPlan = kotlinx.coroutines.flow.MutableStateFlow<StudyPlan?>(null)

    val state: StateFlow<HomeUiState> = combine(base, repository.topics, repository.runningTimer, repository.roadmap, lastPlan) { s, topics, timer, roadmap, plan ->
        s.copy(progress = StudyStats.subjectProgress(topics, s.subjects), runningTimer = timer, roadmap = roadmap, lastPlan = plan)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** 커리큘럼 스케줄링: 복습·로드맵·예습을 앞으로 며칠간의 자습 일정과 할 일로 배치합니다. */
    fun generatePlan(options: PlanOptions) = viewModelScope.launch {
        val s = state.value
        val queue = StudyPlanner.buildQueue(s.progress, s.roadmap, s.subjects)
        val plan = StudyPlanner.generate(queue, s.events, options)
        if (!plan.isEmpty) repository.applyStudyPlan(plan.events, plan.tasks)
        lastPlan.value = plan
    }

    fun dismissPlanResult() { lastPlan.value = null }

    fun setRoadmapStatus(id: String, status: RoadmapStatus) = viewModelScope.launch { repository.setRoadmapStatus(id, status) }

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
