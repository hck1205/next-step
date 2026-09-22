package com.nextstep.app.ui.settings

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.prefs.UserProfile

data class SettingsUiState(
    val profile: UserProfile? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val syncAvailable: Boolean = false,
    /** 이 학생에 연결된 모든 구성원 (학생 본인, 학부모들, 멘토들). */
    val members: List<MemberEntity> = emptyList(),
    val me: MemberEntity? = null,
    val subjects: List<SubjectEntity> = emptyList(),
)
