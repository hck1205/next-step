package com.nextstep.app.ui.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.GoalRepository
import com.nextstep.app.data.repository.JourneyRepository
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.GoalPlanner
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.JourneyPlanner
import com.nextstep.app.domain.journey.MilestoneCategory
import com.nextstep.app.domain.journey.PeriodCalendar
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 구간(학기)별 여정 타임라인. 생년월일 + 카탈로그 + 저장된 이정표 상태 + 목표 단계를 합쳐 보여 주고,
 * 이정표 완료·메모·날짜 변경, 직접 추가, 단계 완료, 단계를 할 일로 보내기를 저장소에 씁니다.
 */
class JourneyViewModel(
    private val streams: FamilyDataStreams,
    private val journey: JourneyRepository,
    private val members: MemberRepository,
    private val goals: GoalRepository,
    private val tasks: TaskRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val filter = MutableStateFlow<MilestoneCategory?>(null)
    private val showCompleted = MutableStateFlow(false)
    private val showPast = MutableStateFlow(false)

    private val base = combine(streams.profile, streams.members, streams.journeyItems, streams.goals, streams.goalSteps) { profile, members, stored, goals, steps ->
        Base(profile, members, stored, goals, steps)
    }

    private val built = combine(base, streams.activities) { (profile, members, stored, goals, steps), activities ->
        val day = today()
        val student = members.firstOrNull { it.role == Role.STUDENT.name }
        val birthDate = student?.birthDate?.let { LocalDate.ofEpochDay(it) }
        val items = JourneyPlanner.build(birthDate, stored, day)
        val periods = birthDate?.let { PeriodCalendar.periods(it) }.orEmpty()
        JourneyUiState(
            studentName = profile.studentName,
            studentMemberId = student?.id,
            stage = GrowthStage.of(members, day),
            ageLabel = birthDate?.let { GrowthStage.ageLabel(it, day) },
            hasBirthDate = birthDate != null,
            today = day,
            items = items,
            periods = periods,
            currentPeriodKey = PeriodCalendar.periodOf(periods, day)?.key,
            goals = goals.filter { !it.deleted },
            steps = steps,
            activities = activities.filter { !it.deleted },
            completion = JourneyPlanner.completion(items, day),
            loaded = true,
        )
    }

    val state: StateFlow<JourneyUiState> = combine(built, filter, showCompleted, showPast) { s, f, c, p ->
        s.copy(filter = f, showCompleted = c, showPast = p)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JourneyUiState())

    fun setStatus(item: JourneyItem, status: MilestoneStatus) = viewModelScope.launch {
        val templateId = item.templateId
        if (templateId != null) journey.setTemplateStatus(templateId, status, item.dueDate)
        else item.entityId?.let { journey.setStatus(it, status) }
    }

    fun setNote(item: JourneyItem, note: String) = viewModelScope.launch {
        val templateId = item.templateId
        if (templateId != null) journey.setTemplateNote(templateId, note, item.dueDate)
        else item.entityId?.let { journey.setNote(it, note) }
    }

    fun setDueDate(item: JourneyItem, dueDate: LocalDate) = viewModelScope.launch {
        val templateId = item.templateId
        if (templateId != null) journey.setTemplateDueDate(templateId, dueDate)
        else item.entityId?.let { journey.setDueDate(it, dueDate) }
    }

    fun addCustom(title: String, description: String, category: MilestoneCategory, dueDate: LocalDate, leadMonths: Int) = viewModelScope.launch {
        journey.addCustom(title, description, category.name, dueDate, leadMonths, priority = 2)
    }

    fun deleteCustom(item: JourneyItem) = viewModelScope.launch { item.entityId?.takeIf { item.isCustom }?.let { journey.delete(it) } }

    fun setFilter(category: MilestoneCategory?) { filter.value = category }
    fun showCompleted(show: Boolean) { showCompleted.value = show }
    fun showPast(show: Boolean) { showPast.value = show }

    fun setBirthDate(date: LocalDate) = viewModelScope.launch { state.value.studentMemberId?.let { members.setBirthDate(it, date) } }

    fun setStepStatus(step: GoalStepEntity, status: MilestoneStatus) = viewModelScope.launch { goals.setStepStatus(step.id, status) }

    /** 단계를 할 일로 보냅니다. 이미 보냈으면 다시 만들지 않습니다. 마감 규칙은 GoalPlanner.taskFor 참고. */
    fun sendStepToTasks(step: GoalStepEntity, createdByRole: String) = viewModelScope.launch {
        if (step.taskId != null) return@launch
        val s = state.value
        val task = GoalPlanner.taskFor(step, s.goals.firstOrNull { it.id == step.goalId }?.title ?: "", s.periods.firstOrNull { it.key == step.periodKey }, s.today, createdByRole)
        tasks.save(task)
        goals.setStepTask(step.id, task.id)
    }

    private data class Base(
        val profile: com.nextstep.app.data.prefs.UserProfile,
        val members: List<com.nextstep.app.data.local.entity.MemberEntity>,
        val stored: List<com.nextstep.app.data.local.entity.JourneyItemEntity>,
        val goals: List<com.nextstep.app.data.local.entity.GoalEntity>,
        val steps: List<GoalStepEntity>,
    )

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: JourneyEvent) {
        when (event) {
            is JourneyEvent.SetStatus -> setStatus(event.item, event.status)
            is JourneyEvent.SetNote -> setNote(event.item, event.note)
            is JourneyEvent.SetDueDate -> setDueDate(event.item, event.dueDate)
            is JourneyEvent.AddCustom -> addCustom(event.title, event.description, event.category, event.dueDate, event.leadMonths)
            is JourneyEvent.DeleteCustom -> deleteCustom(event.item)
            is JourneyEvent.SetFilter -> setFilter(event.category)
            is JourneyEvent.ShowCompleted -> showCompleted(event.show)
            is JourneyEvent.ShowPast -> showPast(event.show)
            is JourneyEvent.SetBirthDate -> setBirthDate(event.date)
            is JourneyEvent.SetStepStatus -> setStepStatus(event.step, event.status)
            is JourneyEvent.SendStepToTasks -> sendStepToTasks(event.step, event.createdByRole)
        }
    }
}
