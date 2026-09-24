package com.nextstep.app.ui.navigation

import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentUiLevel

/**
 * 앱 루트 상태: 프로필과, 역할이 정해졌으면 그 권한. 온보딩 전엔 capabilities 가 null.
 * [studentLevel] 은 학생 기기에서만 있고, 글자 크기·탭·기록하기 항목을 학년에 맞춥니다.
 */
data class RootUiState(val profile: UserProfile, val capabilities: Capabilities?, val studentLevel: StudentUiLevel? = null)
