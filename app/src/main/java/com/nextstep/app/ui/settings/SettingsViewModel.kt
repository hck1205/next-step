package com.nextstep.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.MemberEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.repository.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val profile: UserProfile? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val syncAvailable: Boolean = false,
    /** 이 학생에 연결된 모든 구성원 (학생 본인, 학부모들, 멘토들). */
    val members: List<MemberEntity> = emptyList(),
    val me: MemberEntity? = null,
    val subjects: List<SubjectEntity> = emptyList(),
)

class SettingsViewModel(private val repository: StudyRepository) : ViewModel() {
    val state: StateFlow<SettingsUiState> = combine(repository.profile, repository.sync.status, repository.members, repository.myMember, repository.subjects) { p, s, members, me, subjects ->
        SettingsUiState(p, s, repository.sync.isAvailable, members, me, subjects)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun signOut() = viewModelScope.launch { repository.signOut() }
    fun requestSync() = repository.sync.requestPush()
    fun removeMember(id: String) = viewModelScope.launch { repository.removeMember(id) }
    fun setMySubjects(ids: List<String>) = viewModelScope.launch { state.value.me?.let { repository.setMemberSubjects(it.id, ids) } }
    fun setMentorEnabled(enabled: Boolean) = viewModelScope.launch { state.value.me?.let { repository.setMentorEnabled(it.id, enabled) } }
    fun updateMyProfile(name: String, title: String) = viewModelScope.launch { state.value.me?.let { repository.updateMemberProfile(it.id, name, title) } }
}
