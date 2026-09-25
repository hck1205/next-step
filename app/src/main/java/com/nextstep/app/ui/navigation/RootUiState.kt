package com.nextstep.app.ui.navigation

import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.domain.growth.StudentUiLevel

/**
 * 앱 루트 상태: 프로필과, 역할이 정해졌으면 그 권한. 온보딩 전엔 capabilities 가 null.
 * [studentScreen] 은 학생 기기에서만 있고, 글씨 크기·탭·기록하기·아이 모드(스티커판·아이용 가족·보이는 타이머)를 학년에 맞춥니다.
 */
data class RootUiState(
    val profile: UserProfile,
    val capabilities: Capabilities?,
    val studentScreen: StudentScreen? = null,
) {
    val studentLevel: StudentUiLevel? get() = studentScreen?.level
    /** 학생 글씨 배율: 해마다 조금씩 작아집니다. 학부모·멘토는 1. */
    val studentTextScale: Float get() = studentScreen?.textScale ?: 1f
}
