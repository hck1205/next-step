package com.nextstep.app.ui.goals

import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.journey.JourneyPeriod
import com.nextstep.app.domain.mission.MissionKind
import com.nextstep.app.domain.mission.MissionPlanner
import com.nextstep.app.domain.project.ProjectPlanner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.journey.GoalPlanner
import com.nextstep.app.domain.journey.GoalTrackCatalog
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.nextstep.app.ui.common.asUiState

/**
 * 장기 목표를 구간(학기)별 단계로 쪼개 하나씩 진행합니다. 트랙(카탈로그)에서 시작하거나 직접 만들고,
 * 단계를 완료하거나 할 일로 보냅니다. 단계 계산은 GoalPlanner 가 합니다.
 */
class GoalsViewModel(
    private val streams: FamilyDataStreams,
    private val goals: GoalRepository,
    private val tasks: TaskRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {

    val state: StateFlow<GoalsUiState> = combine(streams.profile, streams.members, streams.goals, streams.goalSteps, streams.subjects) { profile, members, goals, steps, subjects ->
        val day = today()
        val ctx = StudentContext.of(members, day)
        val periods = ctx.periods
        val currentKey = ctx.currentPeriodKey
        // 교육 프로젝트는 같은 목표 저장소를 쓰지만 기록 › 교육 프로젝트에서 따로 봅니다.
        val live = goals.filter { !it.deleted && !ProjectPlanner.isProject(it) }
        val started = live.mapNotNull { it.trackId }.toSet()
        GoalsUiState(
            studentName = profile.studentName,
            hasBirthDate = ctx.hasBirthDate,
            today = day,
            periods = periods,
            currentPeriodKey = currentKey,
            goals = live.map { goal -> viewOf(goal, GoalPlanner.stepsOf(goal, steps), periods, currentKey, day) },
            availableTracks = GoalPlanner.relevantTracks(GoalTrackCatalog.tracks, periods, currentKey).filter { it.id !in started },
            loaded = true,
            missionKinds = MissionKind.forStage(ctx.stage),
            subjectNames = subjects.map { it.name },
        ).let { s ->
            val (open, closed) = s.goals.partition { it.goal.status == GoalStatus.ACTIVE }
            val (missions, longTerm) = open.partition { it.isMission }
            s.copy(missions = missions.sortedBy { it.target }, active = longTerm, finished = closed)
        }
    }.asUiState(viewModelScope, GoalsUiState())

    private fun viewOf(goal: GoalEntity, mine: List<GoalStepEntity>, periods: List<JourneyPeriod>, currentKey: String?, today: LocalDate): GoalView = GoalView(
        goal = goal, steps = mine, progress = GoalPlanner.progress(mine), currentSteps = GoalPlanner.currentSteps(mine, periods, currentKey),
        isComplete = GoalPlanner.isComplete(mine), kind = MissionPlanner.kindOf(goal), target = goal.targetDate?.let { DateUtils.fromEpochDay(it) },
        daysLeft = MissionPlanner.daysLeft(goal, today), nextStep = MissionPlanner.nextStep(mine), overdueSteps = MissionPlanner.overdueCount(mine, today),
    )

    /** 날짜 목표를 만들고 설계된 세부 단계를 목표일에서 거꾸로 배치합니다. */
    fun startMission(kind: MissionKind, target: LocalDate, subject: String?, createdByRole: String) = viewModelScope.launch {
        val s = state.value
        val (goal, steps) = MissionPlanner.create(kind, target, s.today, s.periods, subject?.takeIf { kind.needsSubject }, createdByRole)
        goals.add(goal, steps)
    }

    fun startTrack(trackId: String) = viewModelScope.launch {
        val track = GoalTrackCatalog.byId[trackId] ?: return@launch
        val s = state.value
        if (s.goals.any { it.goal.trackId == trackId }) return@launch
        val goal = GoalEntity(familyId = "", trackId = track.id, title = track.title, area = track.area.name, description = track.description)
        goals.add(goal, GoalPlanner.stepsFor(track, s.periods, goal.id, ""))
    }

    fun addCustomGoal(title: String, area: GoalArea, description: String, stepsByPeriod: List<Pair<String, String>>) = viewModelScope.launch {
        if (title.isBlank()) return@launch
        val goal = GoalEntity(familyId = "", title = title, area = area.name, description = description)
        val steps = stepsByPeriod.filter { it.second.isNotBlank() }.mapIndexed { i, (periodKey, stepTitle) ->
            GoalStepEntity(familyId = "", goalId = goal.id, periodKey = periodKey, orderIndex = i, title = stepTitle)
        }
        goals.add(goal, steps)
    }

    fun addStep(goalId: String, periodKey: String, title: String) = viewModelScope.launch {
        val order = state.value.goals.firstOrNull { it.goal.id == goalId }?.steps?.size ?: 0
        goals.addStep(GoalStepEntity(familyId = "", goalId = goalId, periodKey = periodKey, orderIndex = order, title = title))
    }

    fun setStepStatus(step: GoalStepEntity, status: MilestoneStatus) = viewModelScope.launch {
        goals.setStepStatus(step.id, status)
        val view = state.value.goals.firstOrNull { it.goal.id == step.goalId } ?: return@launch
        val after = view.steps.map { if (it.id == step.id) it.copy(status = status) else it }
        if (GoalPlanner.isComplete(after) && view.goal.status == GoalStatus.ACTIVE) goals.setGoalStatus(view.goal.id, GoalStatus.DONE)
    }

    /** 단계를 할 일로 보냅니다. 이미 보냈으면 다시 만들지 않습니다. 마감 규칙은 GoalPlanner.taskFor 참고. */
    fun sendStepToTasks(step: GoalStepEntity, createdByRole: String) = viewModelScope.launch {
        if (step.taskId != null) return@launch
        val s = state.value
        val view = s.goals.firstOrNull { it.goal.id == step.goalId }
        val type = if (view?.kind?.isExam == true) TaskType.EXAM_PREP else TaskType.OTHER
        val task = GoalPlanner.taskFor(step, view?.goal?.title ?: "", s.periods.firstOrNull { it.key == step.periodKey }, s.today, createdByRole, type)
        tasks.save(task)
        goals.setStepTask(step.id, task.id)
    }

    fun setGoalStatus(goalId: String, status: GoalStatus) = viewModelScope.launch { goals.setGoalStatus(goalId, status) }
    fun deleteGoal(goalId: String) = viewModelScope.launch { goals.delete(goalId) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: GoalsEvent) {
        when (event) {
            is GoalsEvent.StartTrack -> startTrack(event.trackId)
            is GoalsEvent.AddCustomGoal -> addCustomGoal(event.title, event.area, event.description, event.stepsByPeriod)
            is GoalsEvent.StartMission -> startMission(event.kind, event.target, event.subject, event.createdByRole)
            is GoalsEvent.AddStep -> addStep(event.goalId, event.periodKey, event.title)
            is GoalsEvent.SetStepStatus -> setStepStatus(event.step, event.status)
            is GoalsEvent.SendStepToTasks -> sendStepToTasks(event.step, event.createdByRole)
            is GoalsEvent.SetGoalStatus -> setGoalStatus(event.goalId, event.status)
            is GoalsEvent.DeleteGoal -> deleteGoal(event.goalId)
        }
    }
}
