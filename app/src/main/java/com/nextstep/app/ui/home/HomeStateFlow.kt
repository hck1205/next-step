package com.nextstep.app.ui.home

import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.cheer.Cheers
import com.nextstep.app.domain.content.ContentRecommender
import com.nextstep.app.domain.curriculum.CurriculumCatalog
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.family.student
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.familytalk.FamilyTalk
import com.nextstep.app.domain.feedback.FeedbackAudience
import com.nextstep.app.domain.feedback.FeedbackVoice
import com.nextstep.app.domain.gamify.Gamify
import com.nextstep.app.domain.growth.GrowthGuide
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.journey.JourneyPlanner
import com.nextstep.app.domain.mission.MissionPlanner
import com.nextstep.app.domain.planner.StudyPlan
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.reward.Rewards
import com.nextstep.app.domain.selfdirection.SelfDirection
import com.nextstep.app.domain.selfdirection.WeekAccess
import com.nextstep.app.domain.stats.StudyQueues
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.stats.TrendStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.domain.year.AheadPlans
import com.nextstep.app.domain.year.YearPlans
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.common.gameInputs
import com.nextstep.app.ui.common.weekFindings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * 오늘 화면 상태를 기록 흐름에서 차례로 쌓습니다: 기본 수치 → 진도·대기열 → 학생(단계·올해·추천) → 이번 주·미션·루틴 → 게임 → 흐름 차트.
 * ViewModel 은 이 흐름을 구독하고 사용자 동작만 처리합니다.
 */
internal object HomeStateFlow {
    fun of(streams: FamilyDataStreams, lastPlan: Flow<StudyPlan?>): Flow<HomeUiState> {
        val progress = withProgress(base(streams), streams, lastPlan)
        return withFamily(withTrends(withGame(withPlans(withStudent(progress, streams), streams), streams), streams), streams)
    }

    private fun base(streams: FamilyDataStreams): Flow<HomeUiState> =
        combine(streams.profile, streams.subjects, streams.events, streams.tasks, streams.sessions) { profile, subjects, events, tasks, sessions ->
            HomeUiState(
                displayName = profile.displayName,
                subjects = subjects,
                todayEvents = StudyStats.eventsOn(DateUtils.today(), events),
                pendingTasks = StudyStats.pendingTasks(tasks),
                todayMinutes = StudyStats.todayMinutes(sessions),
                weekMinutes = StudyStats.weekMinutes(sessions),
                week = StudyStats.dailyMinutes(sessions, DAYS_IN_WEEK),
                streak = StudyStats.studyStreak(sessions),
                nextExam = StudyStats.upcomingExams(events).firstOrNull(),
                events = events,
                loaded = true,
            )
        }

    private fun withProgress(base: Flow<HomeUiState>, streams: FamilyDataStreams, lastPlan: Flow<StudyPlan?>): Flow<HomeUiState> =
        combine(base, streams.topics, streams.runningTimer, streams.roadmap, lastPlan) { s, topics, timer, roadmap, plan ->
            val progress = StudyStats.subjectProgress(topics, s.subjects)
            s.copy(
                progress = progress, runningTimer = timer, roadmap = roadmap, lastPlan = plan,
                roadmapFocus = StudyQueues.roadmapFocus(roadmap, UiDefaults.MAX_ROWS), activeSubjects = StudyQueues.activeSubjects(progress),
                previewQueue = StudyQueues.previewQueue(progress), reviewQueue = StudyQueues.reviewQueue(progress),
            )
        }

    /** 학생의 화면 단계·올해 프로필·단계가 오른 것·이번 학기·여정·추천. */
    private fun withStudent(progress: Flow<HomeUiState>, streams: FamilyDataStreams): Flow<HomeUiState> =
        combine(progress, streams.contents, streams.grades, streams.members, streams.journeyItems) { s, contents, grades, members, journey ->
            val exams = StudyStats.upcomingExams(s.events)
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
                yearAheadCount = screen.year?.let { YearPlans.ahead(it.key).size } ?: 0,
                yearAheadHeading = screen.year?.let { AheadPlans.heading(it.key) }.orEmpty(),
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
                recommendations = ContentRecommender.recommend(contents, s.subjects, s.progress, StudyStats.subjectScores(grades, s.subjects), exams, gradeLevel = stage?.gradeLevel ?: GradeLevel.ALL, limit = RECOMMENDATIONS),
            )
        }

    /** 나의 이번 주(자기주도 단계가 누가 계획·점검·돌아보기를 하는지 정함) · 미션 · 오늘의 루틴. */
    private fun withPlans(student: Flow<HomeUiState>, streams: FamilyDataStreams): Flow<HomeUiState> {
        val selfWeek = combine(streams.profile, streams.members, streams.myMember, streams.weekPlans, streams.sessions) { profile, all, me, plans, sessions ->
            val day = DateUtils.today()
            val stage = SelfDirection.stageOf(StudentContext.of(all, day).student, day)
            SelfDirection.week(stage, plans, sessions, day) to WeekAccess.of(Capabilities.of(profile.role ?: Role.STUDENT, me), stage)
        }
        return combine(student, streams.goals, streams.goalSteps, streams.projectLogs, selfWeek) { s, goals, steps, logs, (week, access) ->
            s.copy(
                myWeek = week, myWeekAccess = access,
                missionFocus = MissionPlanner.focus(goals, steps, s.today),
                routines = ProjectPlanner.progressAll(goals, steps, logs, s.today).filter { !it.isDone }.take(UiDefaults.MAX_ROWS),
            )
        }
    }

    /** 나의 스티커판·레벨·성장 기록: 모양은 화면 단계(나이)가 정하고, 학부모가 게임 요소를 꺼 두면 계산하지 않습니다. 보상 한 줄은 다음 보상. */
    private fun withGame(planned: Flow<HomeUiState>, streams: FamilyDataStreams): Flow<HomeUiState> =
        combine(planned, streams.members, streams.gameInputs(), streams.rewards) { s, all, input, rewards ->
            if (all.student()?.gamify == false) return@combine s.copy(game = null, nextReward = null)
            val profile = Gamify.profile(input, s.today, style = s.level.game)
            s.copy(game = profile, nextReward = Rewards.next(Rewards.views(rewards, input.goals, profile.level.number, profile.boards)))
        }

    /** 나의 공부 흐름·점수 흐름 카드의 값(내 기록만, 남과 견주지 않음)과 이번 주 한마디(부모·멘토와 같은 사실을 학생의 말로). */
    private fun withTrends(game: Flow<HomeUiState>, streams: FamilyDataStreams): Flow<HomeUiState> =
        combine(game, streams.sessions, streams.grades, streams.weekFindings { DateUtils.today() }) { s, sessions, grades, findings ->
            s.copy(
                studyHeat = TrendStats.heatCalendar(sessions, s.today), scoreSeries = TrendStats.scoreSeries(grades, s.subjects),
                feedback = FeedbackVoice.lines(findings, FeedbackAudience.STUDENT, numbers = s.level.showsNumbers),
            )
        }

    /** 가족 일정: 오늘 것과 미리 보기에 든 다가오는 것(가족 달력), 받은 응원, 주말 이야기 카드. */
    private fun withFamily(trends: Flow<HomeUiState>, streams: FamilyDataStreams): Flow<HomeUiState> =
        combine(trends, streams.familyEvents, streams.members, streams.cheers, streams.weekPlans) { s, family, members, cheers, plans ->
            s.copy(
                familyAhead = FamilyCalendar.ahead(family, s.today), familyMembers = FamilyCalendar.family(members), cheers = Cheers.unseen(cheers),
                talk = FamilyTalk.card(s.today, plans),
            )
        }

    private const val DAYS_IN_WEEK = 7
    private const val RECOMMENDATIONS = 3
}
