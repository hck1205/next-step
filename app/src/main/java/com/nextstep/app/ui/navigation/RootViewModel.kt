package com.nextstep.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.Capabilities
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RootUiState(val profile: UserProfile, val capabilities: Capabilities?)

class RootViewModel(private val repository: StudyRepository) : ViewModel() {
    val state: StateFlow<RootUiState?> = combine(repository.profile, repository.myMember) { profile, me ->
        val role = profile.role
        val caps = role?.let { Capabilities(it, mentorEnabled = it == Role.MENTOR || (me?.mentorEnabled ?: false)) }
        RootUiState(profile, caps)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { repository.resumeSync() }
    }
}
