package com.nextstep.app.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.repository.CheerRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.ProjectRepository
import com.nextstep.app.data.repository.RewardRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.data.repository.WeekPlanRepository
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.cheer.CheerTarget
import com.nextstep.app.domain.cheer.Cheers
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.familytalk.FamilyTalk
import com.nextstep.app.domain.feedback.FeedbackAudience
import com.nextstep.app.domain.feedback.FeedbackEngine
import com.nextstep.app.domain.feedback.FeedbackVoice
import com.nextstep.app.domain.feedback.Finding
import com.nextstep.app.domain.gamify.GameInputs
import com.nextstep.app.domain.gamify.Gamify
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.domain.journey.JourneyPlanner
import com.nextstep.app.domain.mission.MissionPlanner
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.project.RoutineItem
import com.nextstep.app.domain.reward.Rewards
import com.nextstep.app.domain.selfdirection.SelfDirection
import com.nextstep.app.domain.selfdirection.WeekAccess
import com.nextstep.app.domain.stats.BalanceStats
import com.nextstep.app.domain.stats.FamilyTrends
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.stats.TrendStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.common.asUiState
import com.nextstep.app.ui.common.gameInputs
import com.nextstep.app.ui.common.weekFindings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 학부모 첫 화면: 자녀 줄 · 상태 카드 · 챙길 것 · 오늘. 화면이 보여 주는 값만 계산합니다(UX 가이드 3 학부모 화면).
 * 재능·분석·과목 통계는 기록 탭의 ViewModel 이 맡습니다.
 */
class ParentDashboardViewModel(
    private val streams: FamilyDataStreams,
    private val tasks: TaskRepository,
    private val projects: ProjectRepository,
    private val weekPlans: WeekPlanRepository,
    private val rewards: RewardRepository,
    private val cheers: CheerRepository,
    /** 레벨·보상 계산은 학생의 모든 기록으로(학생이 보는 것과 같게). 보이는 프로젝트 범위와 상관없습니다. */
    game: Flow<GameInputs> = streams.gameInputs(),
) : ViewModel() {

    private val core = combine(streams.profile, streams.subjects, streams.sessions, streams.tasks, streams.events) { profile, subjects, sessions, tasks, events ->
        Core(profile, subjects, sessions, tasks, events)
    }

    private val side = combine(streams.members, streams.journeyItems, streams.activities, streams.goals, streams.goalSteps) { members, journey, activities, goals, steps ->
        Side(members, journey, activities, goals, steps)
    }

    private val self = combine(streams.weekPlans, streams.myMember, streams.profile) { plans, me, profile -> Triple(plans, me, profile.role) }

    private val dashboard = combine(core, side, streams.syncStatus, streams.projectLogs, self) { c, x, sync, logs, (plans, me, role) ->
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
            upcomingExams = StudyStats.upcomingExams(c.events).take(UiDefaults.MAX_ROWS),
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
            talk = FamilyTalk.card(today, plans),
        )
    }

    /** 받을 차례가 된 보상: 레벨·스티커판 보상은 기록에서 계산한 지금 값으로 판단합니다(게임 요소를 꺼도 약속은 그대로). 차트 값도 여기서 붙입니다. */
    private val trends = combine(streams.sessions, streams.tasks, streams.grades, streams.subjects) { sessions, tasks, grades, subjects ->
        TrendStats.family(sessions, tasks, grades, subjects, DateUtils.today())
    }

    /** 응원할 수 있는 최근 해낸 일(내가 붙인 응원과 함께). */
    private val cheerTargets = combine(streams.tasks, streams.cheers, streams.profile) { tasks, list, profile ->
        if (!Capabilities.of(profile.role ?: Role.PARENT, null).canCheer) emptyList() else Cheers.targets(tasks, list, profile.memberId.orEmpty(), DateUtils.today())
    }

    /** 차트 값과 이번 주 피드백의 사실(같은 기록에서), 가족 일정, 응원. */
    private val charts = combine(trends, streams.weekFindings { DateUtils.today() }, streams.familyEvents, cheerTargets) { t, f, family, cheer -> Extras(t, f, family, cheer) }

    val state: StateFlow<ParentDashboardUiState> = combine(dashboard, streams.members, game, streams.rewards, charts) { s, members, input, list, (t, findings, family, cheer) ->
        val level = StudentScreen.of(members.firstOrNull { it.isStudent }, s.today).level
        val profile = Gamify.profile(input, s.today, style = level.game)
        val mine = FeedbackEngine.forAudience(findings, FeedbackAudience.PARENT)
        s.copy(
            rewardsDue = Rewards.due(Rewards.views(list, input.goals, profile.level.number, profile.boards)), trends = t,
            feedback = mine.map { FeedbackVoice.line(it, FeedbackAudience.PARENT) },
            feedbackEcho = mine.firstOrNull()?.let { FeedbackVoice.echo(FeedbackVoice.line(it, FeedbackAudience.STUDENT, numbers = level.showsNumbers), s.studentName) },
            familyAhead = FamilyCalendar.ahead(family, s.today), familyMembers = FamilyCalendar.family(members), cheerTargets = cheer,
        )
    }.asUiState(viewModelScope, ParentDashboardUiState())

    private data class Core(
        val profile: UserProfile,
        val subjects: List<SubjectEntity>,
        val sessions: List<StudySessionEntity>,
        val tasks: List<TaskEntity>,
        val events: List<EventEntity>,
    )

    private data class Extras(val trends: FamilyTrends, val findings: List<Finding>, val family: List<FamilyEventEntity>, val cheers: List<CheerTarget>)

    private data class Side(
        val members: List<MemberEntity>,
        val journey: List<JourneyItemEntity>,
        val activities: List<ActivityEntity>,
        val goals: List<GoalEntity>,
        val goalSteps: List<GoalStepEntity>,
    )

    fun toggleRoutine(progress: ProjectProgress, item: RoutineItem) { viewModelScope.launch { projects.toggleRoutine(progress, item, DateUtils.today()) } }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: ParentDashboardEvent) {
        when (event) {
            is ParentDashboardEvent.ToggleRoutine -> toggleRoutine(event.progress, event.item)
            is ParentDashboardEvent.SaveWeekPlan -> viewModelScope.launch { weekPlans.savePlan(DateUtils.weekStart(DateUtils.today()), event.goals, event.minutes) }
            is ParentDashboardEvent.ToggleWeekGoal -> viewModelScope.launch { weekPlans.toggleGoal(event.planId, event.index) }
            is ParentDashboardEvent.ApproveWeek -> viewModelScope.launch { weekPlans.approve(event.planId) }
            is ParentDashboardEvent.GiveReward -> viewModelScope.launch { rewards.give(event.id) }
            is ParentDashboardEvent.Cheer -> viewModelScope.launch {
                cheers.set(event.target.task, Cheers.toggle(event.target.given, event.pressed), streams.myMember.first()?.roleLabel.orEmpty())
            }
            is ParentDashboardEvent.ReflectWeek -> viewModelScope.launch { weekPlans.reflect(event.week, event.mood, event.good, event.hard, event.change) }
        }
    }

}
