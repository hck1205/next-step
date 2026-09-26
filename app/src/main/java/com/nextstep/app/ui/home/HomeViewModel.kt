package com.nextstep.app.ui.home

import com.nextstep.app.domain.growth.StudyKindType
import com.nextstep.app.domain.growth.StudyKind
import com.nextstep.app.domain.growth.StudentScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.distinctUntilChangedBy
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.ProjectRepository
import com.nextstep.app.data.repository.WeekPlanRepository
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.selfdirection.SelfDirection
import com.nextstep.app.domain.selfdirection.WeekAccess
import com.nextstep.app.domain.selfdirection.WeekStatus
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.project.RoutineItem
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.domain.stats.StudyQueues
import com.nextstep.app.domain.mission.MissionPlanner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.repository.ContentRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.RoadmapRepository
import com.nextstep.app.data.repository.StudyPlanRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.data.repository.TopicRepository
import com.nextstep.app.domain.content.ContentRecommender
import com.nextstep.app.domain.planner.PlanOptions
import com.nextstep.app.domain.planner.StudyPlan
import com.nextstep.app.domain.planner.StudyPlanner
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.domain.growth.GrowthGuide
import com.nextstep.app.domain.curriculum.CurriculumCatalog
import com.nextstep.app.domain.journey.JourneyPlanner
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.model.Role
import com.nextstep.app.ui.common.asUiState

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
) : ViewModel() {

    private val base = combine(streams.profile, streams.subjects, streams.events, streams.tasks, streams.sessions) { profile, subjects, events, tasks, sessions ->
        HomeUiState(
            displayName = profile.displayName,
            subjects = subjects,
            todayEvents = StudyStats.eventsOn(DateUtils.today(), events),
            pendingTasks = StudyStats.pendingTasks(tasks),
            todayMinutes = StudyStats.todayMinutes(sessions),
            weekMinutes = StudyStats.weekMinutes(sessions),
            weekGoalMinutes = subjects.sumOf { it.weeklyGoalMinutes },
            week = StudyStats.dailyMinutes(sessions, DAYS_IN_WEEK),
            streak = StudyStats.studyStreak(sessions),
            nextExam = StudyStats.upcomingExams(events, tasks).firstOrNull(),
            events = events,
            loaded = true,
        )
    }

    private val lastPlan = MutableStateFlow<StudyPlan?>(null)

    private val withProgress = combine(base, streams.topics, streams.runningTimer, streams.roadmap, lastPlan) { s, topics, timer, roadmap, plan ->
        val progress = StudyStats.subjectProgress(topics, s.subjects)
        s.copy(
            progress = progress, runningTimer = timer, roadmap = roadmap, lastPlan = plan,
            roadmapFocus = StudyQueues.roadmapFocus(roadmap, UiDefaults.MAX_ROWS), activeSubjects = StudyQueues.activeSubjects(progress),
            previewQueue = StudyQueues.previewQueue(progress), reviewQueue = StudyQueues.reviewQueue(progress),
        )
    }

    private val enriched = combine(withProgress, streams.contents, streams.grades, streams.members, streams.journeyItems) { s, contents, grades, members, journey ->
        val exams = StudyStats.upcomingExams(s.events, emptyList())
        val today = DateUtils.today()
        val ctx = StudentContext.of(members, today)
        val stage = ctx.stage
        val screen = StudentScreen.of(ctx.student, today)
        val level = screen.level
        val seen = StudentUiLevel.fromName(ctx.student?.seenUiLevel)
        val levelUp = level.takeIf { seen != null && it > seen }
        s.copy(
            level = level,
            year = screen.year,
            taskRows = screen.taskRows,
            homeOrder = screen.homeOrder,
            levelUp = levelUp,
            newSections = if (levelUp != null && seen != null) level.newSince(seen) else emptyList(),
            studentId = ctx.student?.id,
            curriculum = CurriculumCatalog.forPeriod(ctx.currentPeriodKey),
            periodLabel = ctx.currentPeriod?.label,
            journeyNow = JourneyPlanner.actionable(JourneyPlanner.build(ctx.birthDate, journey, today), today),
            hasBirthDate = ctx.hasBirthDate,
            today = today,
            stage = stage,
            planDefaults = GrowthGuide.defaultPlanOptions(stage, screen.year),
            recommendations = ContentRecommender.recommend(contents, s.subjects, s.progress, StudyStats.subjectScores(grades, s.subjects), exams, gradeLevel = stage?.gradeLevel ?: GradeLevel.ALL, limit = 3),
        )
    }

    /** 나의 이번 주: 자기주도 단계가 누가 계획·점검·돌아보기를 하는지 정합니다. */
    private val selfWeek = combine(streams.profile, streams.members, streams.myMember, streams.weekPlans, streams.sessions) { profile, all, me, plans, sessions ->
        val day = DateUtils.today()
        val stage = SelfDirection.stageOf(StudentContext.of(all, day).student, day)
        SelfWeek(SelfDirection.week(stage, plans, sessions, day), WeekAccess.of(Capabilities.of(profile.role ?: Role.STUDENT, me), stage))
    }

    val state: StateFlow<HomeUiState> = combine(enriched, streams.goals, streams.goalSteps, streams.projectLogs, selfWeek) { s, goals, steps, logs, w ->
        s.copy(
            myWeek = w.week, myWeekAccess = w.access,
            missionFocus = MissionPlanner.focus(goals, steps, s.today),
            routines = ProjectPlanner.progressAll(goals, steps, logs, s.today).filter { !it.isDone }.take(UiDefaults.MAX_ROWS),
        )
    }
        .asUiState(viewModelScope, HomeUiState())

    init {
        // 처음 여는 학생 기기: 지금 단계를 확인한 것으로 조용히 남겨, 다음 학년에 올라갈 때만 "새 화면" 카드가 뜨게 합니다.
        viewModelScope.launch {
            streams.members.map { list -> list.firstOrNull { it.isStudent } }
                .filterNotNull().filter { it.seenUiLevel.isBlank() }.distinctUntilChangedBy { it.id }
                .collect { members.markUiLevelSeen(it.id, StudentUiLevel.of(it)) }
        }
    }

    fun dismissLevelUp() = viewModelScope.launch {
        val s = state.value
        s.studentId?.let { members.markUiLevelSeen(it, s.level) }
    }

    fun markContentWatched(id: String) = viewModelScope.launch { contents.setWatched(id, true) }

    /** 커리큘럼 스케줄링: 복습·로드맵·예습을 앞으로 며칠간의 자습 일정과 할 일로 배치합니다. */
    fun generatePlan(options: PlanOptions) = viewModelScope.launch {
        val s = state.value
        val queue = StudyPlanner.buildQueue(s.progress, s.roadmap, s.subjects)
        val plan = StudyPlanner.generate(queue, s.events, options)
        plans.apply(plan)
        lastPlan.value = plan
    }

    fun dismissPlanResult() { lastPlan.value = null }

    fun setRoadmapStatus(id: String, status: RoadmapStatus) = viewModelScope.launch { roadmap.setStatus(id, status) }

    fun toggleTask(task: TaskEntity) = viewModelScope.launch { tasks.setDone(task.id, !task.done) }

    fun markTopic(topic: TopicEntity, status: TopicStatus) = viewModelScope.launch { topics.setStatus(topic.id, status) }

    fun addQuickTask(subject: SubjectEntity, topic: TopicEntity, type: TaskType) = viewModelScope.launch {
        tasks.save(
            TaskEntity(
                familyId = "", subjectId = subject.id, topicId = topic.id,
                title = "${subject.name} ${topic.title} ${type.label}", type = type,
                dueDate = DateUtils.today().toEpochDay(), createdByRole = Role.STUDENT.name,
            ),
        )
    }

    /** 올해의 공부 한 가지를 그 분량의 오늘 할 일로 만듭니다. 시험 준비는 시험 준비 종류로. */
    fun addStudyKind(kind: StudyKind) = viewModelScope.launch {
        tasks.save(
            TaskEntity(
                familyId = "", title = "${kind.name} ${kind.minutes}분",
                type = if (kind.type == StudyKindType.TEST_PREP) TaskType.EXAM_PREP else TaskType.HOMEWORK,
                dueDate = DateUtils.today().toEpochDay(), createdByRole = Role.STUDENT.name,
            ),
        )
    }

    fun toggleRoutine(progress: ProjectProgress, item: RoutineItem) = viewModelScope.launch { projects.toggleRoutine(progress, item, state.value.today) }

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
        }
    }

    private data class SelfWeek(val week: WeekStatus, val access: WeekAccess)

    private companion object {
        const val DAYS_IN_WEEK = 7
    }
}
