package com.nextstep.app.data.prefs

import com.nextstep.app.data.model.Role

/** 온보딩 이후 기기에 저장되는 사용자/가족 정보. */
data class UserProfile(
    val role: Role?,
    val displayName: String,
    val familyId: String?,
    val pairingCode: String?,
    val studentName: String,
    val onboarded: Boolean,
    /** 이 기기 사용자의 구성원(MemberEntity) ID. */
    val memberId: String?,
)
