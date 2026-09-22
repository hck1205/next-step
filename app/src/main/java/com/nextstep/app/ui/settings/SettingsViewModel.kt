package com.nextstep.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.nextstep.app.data.model.Role

class SettingsViewModel(
    private val streams: FamilyDataStreams,
    private val onboarding: OnboardingRepository,
    private val members: MemberRepository,
) : ViewModel() {
    val state: StateFlow<SettingsUiState> = combine(streams.profile, streams.syncStatus, streams.members, streams.myMember, streams.subjects) { p, s, members, me, subjects ->
        SettingsUiState(p, s, onboarding.syncAvailable, members, me, subjects)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun signOut() = viewModelScope.launch { onboarding.signOut() }
    fun requestSync() = onboarding.requestSync()
    fun removeMember(id: String) = viewModelScope.launch { members.remove(id) }
    fun setMySubjects(ids: List<String>) = viewModelScope.launch { state.value.me?.let { members.setSubjects(it.id, ids) } }
    fun setMentorEnabled(enabled: Boolean) = viewModelScope.launch { state.value.me?.let { members.setMentorEnabled(it.id, enabled) } }
    fun setGradeYear(gradeYear: Int) = viewModelScope.launch { state.value.members.firstOrNull { it.role == Role.STUDENT.name }?.let { members.setGradeYear(it.id, gradeYear) } }
    fun setBirthDate(date: java.time.LocalDate?) = viewModelScope.launch { state.value.members.firstOrNull { it.role == Role.STUDENT.name }?.let { members.setBirthDate(it.id, date) } }
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
            is SettingsEvent.SetBirthDate -> setBirthDate(event.date)
        }
    }

}
