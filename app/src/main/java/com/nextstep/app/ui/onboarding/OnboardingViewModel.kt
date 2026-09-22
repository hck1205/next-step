package com.nextstep.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val step: Int = 0,
    val role: Role? = null,
    val name: String = "",
    val code: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val syncAvailable: Boolean = false,
)

class OnboardingViewModel(private val repository: StudyRepository) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUiState(syncAvailable = repository.sync.isAvailable))
    val state: StateFlow<OnboardingUiState> = _state

    fun selectRole(role: Role) = _state.update { it.copy(role = role, step = 1, error = null) }
    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setCode(v: String) = _state.update { it.copy(code = v.uppercase().take(6)) }
    fun back() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0), error = null) }

    fun submit() {
        val s = _state.value
        val role = s.role ?: return
        if (s.name.isBlank()) { _state.update { it.copy(error = "이름을 입력해 주세요") }; return }
        if (role == Role.PARENT && s.code.length < 6) { _state.update { it.copy(error = "6자리 연결 코드를 입력해 주세요") }; return }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = if (role == Role.STUDENT) repository.createFamilyAsStudent(s.name.trim())
            else repository.joinFamilyAsParent(s.name.trim(), s.code)
            result.onFailure { e -> _state.update { it.copy(loading = false, error = e.message ?: "오류가 발생했습니다") } }
            // 성공 시 RootViewModel 이 profile 변경을 감지해 메인 화면으로 전환합니다.
        }
    }
}
