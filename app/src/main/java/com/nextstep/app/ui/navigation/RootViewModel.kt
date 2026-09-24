package com.nextstep.app.ui.navigation

import com.nextstep.app.domain.growth.StudentUiLevel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.OnboardingRepository
import com.nextstep.app.domain.access.Capabilities
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 루트 내비게이션이 쓰는 프로필·권한. 화면 전환 중에도 유지돼야 하므로 Eagerly 로 공유합니다. */
class RootViewModel(
    private val onboarding: OnboardingRepository,
    private val members: MemberRepository,
) : ViewModel() {
    val state: StateFlow<RootUiState?> = combine(onboarding.profile, members.myMember) { profile, me ->
        val caps = profile.role?.let { Capabilities.of(it, me) }
        RootUiState(profile, caps, studentLevel = me?.takeIf { caps?.isStudent == true }?.let { StudentUiLevel.of(it) })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { onboarding.resumeSync() }
    }

    /** 다자녀·다학생: 다른 자녀의 공간으로 전환합니다. 모든 화면의 스트림이 가족 ID 를 따라 바뀝니다. */
    fun switchChild(familyId: String) = viewModelScope.launch { onboarding.switchChild(familyId) }
}
