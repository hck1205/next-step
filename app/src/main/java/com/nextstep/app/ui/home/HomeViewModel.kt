package com.nextstep.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.repository.CheerRepository
import com.nextstep.app.data.repository.ContentRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.ProjectRepository
import com.nextstep.app.data.repository.RoadmapRepository
import com.nextstep.app.data.repository.StudyPlanRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.data.repository.TopicRepository
import com.nextstep.app.data.repository.WeekPlanRepository
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.growth.StudyKind
import com.nextstep.app.domain.planner.PlanOptions
import com.nextstep.app.domain.planner.StudyPlan
import com.nextstep.app.domain.planner.StudyPlanner
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.project.RoutineItem
import com.nextstep.app.domain.task.TaskDrafts
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeViewModel(
    private val streams: FamilyDataStreams,
    private val tasks: TaskRepository,
    private val topics: TopicRepository,
    private val roadmap: RoadmapRepository,
    private val contents: ContentRepository,
    private val plans: StudyPlanRepository,
    private val members: MemberRepository,
    private val projects: ProjectRepository,
    private val weekPlans: WeekPlanRepository,
    private val cheers: CheerRepository,
) : ViewModel() {

    private val lastPlan = MutableStateFlow<StudyPlan?>(null)

    val state: StateFlow<HomeUiState> = HomeStateFlow.of(streams, lastPlan).asUiState(viewModelScope, HomeUiState())

    init {
        // 처음 여는 학생 기기: 지금 단계를 확인한 것으로 조용히 남겨, 다음 학년에 올라갈 때만 "새 화면" 카드가 뜨게 합니다.
        viewModelScope.launch {
            streams.members.map { list -> list.firstOrNull { it.isStudent } }
                .filterNotNull().filter { it.seenUiLevel.isBlank() }.distinctUntilChangedBy { it.id }
                .collect { members.markUiLevelSeen(it.id, StudentUiLevel.of(it)) }
        }
    }

    fun dismissLevelUp() {
        viewModelScope.launch {
            val s = state.value
            s.studentId?.let { members.markUiLevelSeen(it, s.level) }
        }
    }

    fun markContentWatched(id: String) { viewModelScope.launch { contents.setWatched(id, true) } }

    /** 커리큘럼 스케줄링: 복습·로드맵·예습을 앞으로 며칠간의 자습 일정과 할 일로 배치합니다. */
    fun generatePlan(options: PlanOptions) {
        viewModelScope.launch {
            val s = state.value
            val queue = StudyPlanner.buildQueue(s.progress, s.roadmap, s.subjects)
            val plan = StudyPlanner.generate(queue, s.events, options)
            plans.apply(plan)
            lastPlan.value = plan
        }
    }

    fun dismissPlanResult() { lastPlan.value = null }

    fun setRoadmapStatus(id: String, status: RoadmapStatus) { viewModelScope.launch { roadmap.setStatus(id, status) } }

    fun toggleTask(task: TaskEntity) { viewModelScope.launch { tasks.setDone(task.id, !task.done) } }

    fun markTopic(topic: TopicEntity, status: TopicStatus) { viewModelScope.launch { topics.setStatus(topic.id, status) } }

    fun addQuickTask(subject: SubjectEntity, topic: TopicEntity, type: TaskType) {
        viewModelScope.launch {
            tasks.save(TaskDrafts.forTopic(subject, topic, type, DateUtils.today(), Role.STUDENT.name))
        }
    }

    /** 올해의 공부 한 가지를 그 분량의 오늘 할 일로 만듭니다. 시험 준비는 시험 준비 종류로. */
    fun addStudyKind(kind: StudyKind) { viewModelScope.launch { tasks.save(TaskDrafts.forStudyKind(kind, DateUtils.today())) } }

    fun toggleRoutine(progress: ProjectProgress, item: RoutineItem) { viewModelScope.launch { projects.toggleRoutine(progress, item, state.value.today) } }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.MarkContentWatched -> markContentWatched(event.id)
            is HomeEvent.GeneratePlan -> generatePlan(event.options)
            HomeEvent.DismissPlanResult -> dismissPlanResult()
            is HomeEvent.SetRoadmapStatus -> setRoadmapStatus(event.id, event.status)
            is HomeEvent.ToggleTask -> toggleTask(event.task)
            is HomeEvent.MarkTopic -> markTopic(event.topic, event.status)
            is HomeEvent.AddQuickTask -> addQuickTask(event.subject, event.topic, event.type)
            HomeEvent.DismissLevelUp -> dismissLevelUp()
            is HomeEvent.AddStudyKind -> addStudyKind(event.kind)
            is HomeEvent.ToggleRoutine -> toggleRoutine(event.progress, event.item)
            is HomeEvent.SaveWeekPlan -> viewModelScope.launch { weekPlans.savePlan(DateUtils.weekStart(state.value.today), event.goals, event.minutes) }
            is HomeEvent.ToggleWeekGoal -> viewModelScope.launch { weekPlans.toggleGoal(event.planId, event.index) }
            is HomeEvent.ReflectWeek -> viewModelScope.launch { weekPlans.reflect(event.week, event.mood, event.good, event.hard, event.change) }
            is HomeEvent.ThankCheers -> viewModelScope.launch { cheers.markSeen(event.ids) }
        }
    }
}
