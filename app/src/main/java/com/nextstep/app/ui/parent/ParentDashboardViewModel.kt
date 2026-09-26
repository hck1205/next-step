package com.nextstep.app.ui.parent

import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.domain.mission.MissionPlanner
import com.nextstep.app.domain.goaltree.GoalTree
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.ProjectRepository
import com.nextstep.app.data.repository.WeekPlanRepository
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.selfdirection.SelfDirection
import com.nextstep.app.domain.selfdirection.WeekAccess
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.project.RoutineItem
import com.nextstep.app.domain.stats.StudyStats
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.domain.journey.JourneyPlanner
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.stats.BalanceStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.common.asUiState

/**
 * 학부모 첫 화면: 자녀 줄 · 상태 카드 · 챙길 것 · 오늘. 화면이 보여 주는 값만 계산합니다(UX 가이드 3 학부모 화면).
 * 재능·분석·과목 통계는 기록 탭의 ViewModel 이 맡습니다.
 */
class ParentDashboardViewModel(
    private val streams: FamilyDataStreams,
    private val tasks: TaskRepository,
    private val projects: ProjectRepository,
    private val weekPlans: WeekPlanRepository,
) : ViewModel() {

    private val core = combine(streams.profile, streams.subjects, streams.sessions, streams.tasks, streams.events) { profile, subjects, sessions, tasks, events ->
        Core(profile, subjects, sessions, tasks, events)
    }

    private val side = combine(streams.members, streams.journeyItems, streams.activities, streams.goals, streams.goalSteps) { members, journey, activities, goals, steps ->
        Side(members, journey, activities, goals, steps)
    }

    private val self = combine(streams.weekPlans, streams.myMember, streams.profile) { plans, me, profile -> Triple(plans, me, profile.role) }

    val state: StateFlow<ParentDashboardUiState> = combine(core, side, streams.syncStatus, streams.projectLogs, self) { c, x, sync, logs, (plans, me, role) ->
        val today = DateUtils.today()
        val ctx = StudentContext.of(x.members, today)
        val selfStage = SelfDirection.stageOf(ctx.student, today)
        val period = ctx.currentPeriod
        ParentDashboardUiState(
            studentName = c.profile.studentName,
            children = c.profile.children,
            activeFamilyId = c.profile.familyId,
            syncStatus = sync,
            subjects = c.subjects,
            weekMinutes = StudyStats.weekMinutes(c.sessions),
            streak = StudyStats.studyStreak(c.sessions),
            pendingTasks = StudyStats.pendingTasks(c.tasks),
            overdueCount = StudyStats.overdueTasks(c.tasks).size,
            upcomingExams = StudyStats.upcomingExams(c.events, c.tasks).take(UiDefaults.MAX_ROWS),
            todayEvents = StudyStats.eventsOn(today, c.events),
            stage = ctx.stage,
            periodLabel = period?.label,
            hasBirthDate = ctx.hasBirthDate,
            today = today,
            balance = BalanceStats.report(ctx.stage, c.sessions, c.tasks, x.activities, period, today, c.events, ctx.year),
            journeyNow = JourneyPlanner.actionable(JourneyPlanner.build(ctx.birthDate, x.journey, today), today),
            missionFocus = MissionPlanner.focus(x.goals, x.goalSteps, today),
            goalFocus = GoalTree.focus(GoalTree.nodes(x.goals, c.tasks, today)),
            week = SelfDirection.week(selfStage, plans, c.sessions, today),
            weekAccess = WeekAccess.of(Capabilities.of(role ?: Role.PARENT, me), selfStage),
            routines = ProjectPlanner.progressAll(x.goals, x.goalSteps, logs, today).filter { !it.isDone }.take(UiDefaults.MAX_ROWS),
        )
    }.asUiState(viewModelScope, ParentDashboardUiState())

    /** 학부모가 자녀에게 할 일을 배정합니다. */
    fun assignTask(title: String, subjectId: String?, type: TaskType, due: LocalDate, createdByRole: String) = viewModelScope.launch {
        tasks.save(TaskEntity(familyId = "", subjectId = subjectId, title = title, type = type, dueDate = due.toEpochDay(), createdByRole = createdByRole))
    }

    private data class Core(
        val profile: UserProfile,
        val subjects: List<SubjectEntity>,
        val sessions: List<StudySessionEntity>,
        val tasks: List<TaskEntity>,
        val events: List<EventEntity>,
    )

    private data class Side(
        val members: List<MemberEntity>,
        val journey: List<JourneyItemEntity>,
        val activities: List<ActivityEntity>,
        val goals: List<GoalEntity>,
        val goalSteps: List<GoalStepEntity>,
    )

    fun toggleRoutine(progress: ProjectProgress, item: RoutineItem) = viewModelScope.launch {
        val phase = progress.current ?: return@launch
        projects.toggle(progress.goalId, phase.key, item.name, item.minutes, DateUtils.today().toEpochDay())
    }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: ParentDashboardEvent) {
        when (event) {
            is ParentDashboardEvent.AssignTask -> assignTask(event.title, event.subjectId, event.type, event.due, event.createdByRole)
            is ParentDashboardEvent.ToggleRoutine -> toggleRoutine(event.progress, event.item)
            is ParentDashboardEvent.SaveWeekPlan -> viewModelScope.launch { weekPlans.savePlan(SelfDirection.weekStart(DateUtils.today()), event.goals, event.minutes) }
            is ParentDashboardEvent.ToggleWeekGoal -> viewModelScope.launch { weekPlans.toggleGoal(event.planId, event.index) }
            is ParentDashboardEvent.ApproveWeek -> viewModelScope.launch { weekPlans.approve(event.planId) }
            is ParentDashboardEvent.ReflectWeek -> viewModelScope.launch { weekPlans.reflect(event.week, event.mood, event.good, event.hard, event.change) }
        }
    }

}
