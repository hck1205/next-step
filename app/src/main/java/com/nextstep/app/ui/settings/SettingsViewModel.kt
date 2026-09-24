package com.nextstep.app.ui.settings

import com.nextstep.app.domain.growth.StudentUiLevel
import kotlinx.coroutines.flow.MutableStateFlow
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.ui.common.asUiState

class SettingsViewModel(
    private val streams: FamilyDataStreams,
    private val onboarding: OnboardingRepository,
    private val members: MemberRepository,
) : ViewModel() {
    private val childError = MutableStateFlow<String?>(null)

    val state: StateFlow<SettingsUiState> = combine(streams.profile, streams.syncStatus, streams.members, streams.myMember, streams.subjects) { p, s, members, me, subjects ->
        val student = members.firstOrNull { it.isStudent }
        val birth = student?.birthDate?.let { DateUtils.fromEpochDay(it) }
        SettingsUiState(
            p, s, onboarding.syncAvailable, members, me, subjects, student, birth, birth?.let { GrowthStage.ageLabel(it, DateUtils.today()) },
            autoStudentLevel = student?.let { StudentUiLevel.auto(it) },
            chosenStudentLevel = StudentUiLevel.fromName(student?.uiLevel),
        )
    }.combine(childError) { s, e -> s.copy(childError = e) }
        .asUiState(viewModelScope, SettingsUiState())

    fun switchChild(familyId: String) = viewModelScope.launch { onboarding.switchChild(familyId) }
    fun addChild(name: String, birthDate: LocalDate?) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        onboarding.addChildAsParent(name.trim(), birthDate).onFailure { childError.value = it.message }
    }
    fun linkChild(code: String) = viewModelScope.launch {
        if (code.isBlank()) return@launch
        onboarding.linkChild(code).onFailure { childError.value = it.message }
    }

    fun signOut() = viewModelScope.launch { onboarding.signOut() }
    fun requestSync() = onboarding.requestSync()
    fun removeMember(id: String) = viewModelScope.launch { members.remove(id) }
    fun setMySubjects(ids: List<String>) = viewModelScope.launch { state.value.me?.let { members.setSubjects(it.id, ids) } }
    fun setMentorEnabled(enabled: Boolean) = viewModelScope.launch { state.value.me?.let { members.setMentorEnabled(it.id, enabled) } }
    fun setGradeYear(gradeYear: Int) = viewModelScope.launch { state.value.members.firstOrNull { it.isStudent }?.let { members.setGradeYear(it.id, gradeYear) } }
    fun setBirthDate(date: LocalDate?) = viewModelScope.launch { state.value.members.firstOrNull { it.isStudent }?.let { members.setBirthDate(it.id, date) } }
    fun setStudentLevel(level: StudentUiLevel?) = viewModelScope.launch { state.value.student?.let { members.setUiLevel(it.id, level) } }
    fun updateMyProfile(name: String, title: String) = viewModelScope.launch { state.value.me?.let { members.updateProfile(it.id, name, title) } }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.SignOut -> signOut()
            SettingsEvent.RequestSync -> requestSync()
            is SettingsEvent.RemoveMember -> removeMember(event.id)
            is SettingsEvent.SetMySubjects -> setMySubjects(event.ids)
            is SettingsEvent.SetMentorEnabled -> setMentorEnabled(event.enabled)
            is SettingsEvent.UpdateMyProfile -> updateMyProfile(event.name, event.title)
            is SettingsEvent.SetGradeYear -> setGradeYear(event.gradeYear)
            is SettingsEvent.SetStudentLevel -> setStudentLevel(event.level)
            is SettingsEvent.SetBirthDate -> setBirthDate(event.date)
            is SettingsEvent.SwitchChild -> switchChild(event.familyId)
            is SettingsEvent.AddChild -> addChild(event.name, event.birthDate)
            is SettingsEvent.LinkChild -> linkChild(event.code)
            SettingsEvent.DismissChildError -> childError.value = null
        }
    }

}
