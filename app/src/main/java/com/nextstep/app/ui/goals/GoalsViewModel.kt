package com.nextstep.app.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.journey.GoalPlanner
import com.nextstep.app.domain.journey.GoalTrackCatalog
import com.nextstep.app.domain.journey.PeriodCalendar
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

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

    val state: StateFlow<GoalsUiState> = combine(streams.profile, streams.members, streams.goals, streams.goalSteps) { profile, members, goals, steps ->
        val day = today()
        val birthDate = members.firstOrNull { it.role == Role.STUDENT.name }?.birthDate?.let { LocalDate.ofEpochDay(it) }
        val periods = birthDate?.let { PeriodCalendar.periods(it) }.orEmpty()
        val currentKey = PeriodCalendar.periodOf(periods, day)?.key
        val live = goals.filter { !it.deleted }
        val started = live.mapNotNull { it.trackId }.toSet()
        GoalsUiState(
            studentName = profile.studentName,
            hasBirthDate = birthDate != null,
            today = day,
            periods = periods,
            currentPeriodKey = currentKey,
            goals = live.map { goal ->
                val mine = GoalPlanner.stepsOf(goal, steps)
                GoalView(goal, mine, GoalPlanner.progress(mine), GoalPlanner.currentSteps(mine, periods, currentKey), GoalPlanner.isComplete(mine))
            },
            availableTracks = GoalPlanner.relevantTracks(GoalTrackCatalog.tracks, periods, currentKey).filter { it.id !in started },
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

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
        val task = GoalPlanner.taskFor(step, s.goals.firstOrNull { it.goal.id == step.goalId }?.goal?.title ?: "", s.periods.firstOrNull { it.key == step.periodKey }, s.today, createdByRole)
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
            is GoalsEvent.AddStep -> addStep(event.goalId, event.periodKey, event.title)
            is GoalsEvent.SetStepStatus -> setStepStatus(event.step, event.status)
            is GoalsEvent.SendStepToTasks -> sendStepToTasks(event.step, event.createdByRole)
            is GoalsEvent.SetGoalStatus -> setGoalStatus(event.goalId, event.status)
            is GoalsEvent.DeleteGoal -> deleteGoal(event.goalId)
        }
    }
}
