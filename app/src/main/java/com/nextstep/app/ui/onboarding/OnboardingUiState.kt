package com.nextstep.app.ui.onboarding

import com.nextstep.app.data.model.Role

data class OnboardingUiState(
    val step: Int = 0,
    val role: Role? = null,
    val name: String = "",
    val code: String = "",
    /** 멘토 구분 (예: 수학 과외). */
    val title: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val syncAvailable: Boolean = false,
)
