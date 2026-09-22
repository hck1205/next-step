package com.nextstep.app.ui.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.JourneyRepository
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.JourneyPlanner
import com.nextstep.app.domain.journey.MilestoneCategory
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 신생아부터 대학원까지의 여정 타임라인. 생년월일 + 카탈로그 + 저장된 상태를 합쳐 보여 주고,
 * 완료·건너뛰기·메모·날짜 변경과 직접 추가를 저장소에 씁니다.
 * 카탈로그 항목은 templateId 로, 직접 추가 항목은 entityId 로 저장소에 전달합니다.
 */
class JourneyViewModel(
    private val streams: FamilyDataStreams,
    private val journey: JourneyRepository,
    private val members: MemberRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val filter = MutableStateFlow<MilestoneCategory?>(null)
    private val showCompleted = MutableStateFlow(false)

    val state: StateFlow<JourneyUiState> = combine(streams.profile, streams.members, streams.journeyItems, filter, showCompleted) { profile, members, stored, filter, showCompleted ->
        val day = today()
        val student = members.firstOrNull { it.role == Role.STUDENT.name }
        val birthDate = student?.birthDate?.let { LocalDate.ofEpochDay(it) }
        val items = JourneyPlanner.build(birthDate, stored, day)
        JourneyUiState(
            studentName = profile.studentName,
            studentMemberId = student?.id,
            stage = GrowthStage.of(members, day),
            ageLabel = birthDate?.let { GrowthStage.ageLabel(it, day) },
            hasBirthDate = birthDate != null,
            today = day,
            items = items,
            completion = JourneyPlanner.completion(items, day),
            filter = filter,
            showCompleted = showCompleted,
            loaded = true,
        )
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

    fun setBirthDate(date: LocalDate) = viewModelScope.launch { state.value.studentMemberId?.let { members.setBirthDate(it, date) } }

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
            is JourneyEvent.SetBirthDate -> setBirthDate(event.date)
        }
    }
}
