package com.nextstep.app.ui.onboarding

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.model.Role

/** Onboarding 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface OnboardingEvent {
    data class SelectRole(val role: Role) : OnboardingEvent
    data class SetName(val v: String) : OnboardingEvent
    data class SetCode(val v: String) : OnboardingEvent
    data class SetTitle(val v: String) : OnboardingEvent
    data object Back : OnboardingEvent
    data object Submit : OnboardingEvent
}
