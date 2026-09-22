package com.nextstep.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
)

class SettingsViewModel(private val repository: StudyRepository) : ViewModel() {
    val state: StateFlow<SettingsUiState> = combine(repository.profile, repository.sync.status) { p, s ->
        SettingsUiState(p, s, repository.sync.isAvailable)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun signOut() = viewModelScope.launch { repository.signOut() }
    fun requestSync() = repository.sync.requestPush()
}
