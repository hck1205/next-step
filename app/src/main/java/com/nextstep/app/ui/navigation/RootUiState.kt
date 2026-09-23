package com.nextstep.app.ui.navigation

import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.domain.access.Capabilities

/** 앱 루트 상태: 프로필과, 역할이 정해졌으면 그 권한. 온보딩 전엔 capabilities 가 null. */
data class RootUiState(val profile: UserProfile, val capabilities: Capabilities?)
