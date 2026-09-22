package com.nextstep.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val onboarding: OnboardingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUiState(syncAvailable = onboarding.syncAvailable))
    val state: StateFlow<OnboardingUiState> = _state

    fun selectRole(role: Role) = _state.update { it.copy(role = role, step = 1, error = null) }
    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setCode(v: String) = _state.update { it.copy(code = v.uppercase().take(6)) }
    fun setTitle(v: String) = _state.update { it.copy(title = v) }
    fun back() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0), error = null) }

    fun submit() {
        val s = _state.value
        val role = s.role ?: return
        if (s.name.isBlank()) { _state.update { it.copy(error = "이름을 입력해 주세요") }; return }
        if (role != Role.STUDENT && s.code.length < 6) { _state.update { it.copy(error = "6자리 연결 코드를 입력해 주세요") }; return }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = if (role == Role.STUDENT) onboarding.createFamilyAsStudent(s.name.trim())
            else onboarding.joinFamily(role, s.name.trim(), s.code, s.title.trim())
            result.onFailure { e -> _state.update { it.copy(loading = false, error = e.message ?: "오류가 발생했습니다") } }
            // 성공 시 RootViewModel 이 profile 변경을 감지해 메인 화면으로 전환합니다.
        }
    }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.SelectRole -> selectRole(event.role)
            is OnboardingEvent.SetName -> setName(event.v)
            is OnboardingEvent.SetCode -> setCode(event.v)
            is OnboardingEvent.SetTitle -> setTitle(event.v)
            OnboardingEvent.Back -> back()
            OnboardingEvent.Submit -> submit()
        }
    }

}
