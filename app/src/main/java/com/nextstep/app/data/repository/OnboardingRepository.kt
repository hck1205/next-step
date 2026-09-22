package com.nextstep.app.data.repository

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.sync.FamilyInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** 역할 선택, 가족 생성·참여, 동기화 생명주기. */
interface OnboardingRepository {
    val profile: Flow<UserProfile>
    val syncStatus: StateFlow<SyncStatus>
    val syncAvailable: Boolean

    /** 학생: 새 가족을 만들고 연결 코드를 발급합니다. */
    suspend fun createFamilyAsStudent(studentName: String, gradeYear: Int = 0): Result<FamilyInfo>

    /** 학부모·멘토: 연결 코드로 학생의 가족에 참여합니다. 같은 코드로 여러 명이 참여할 수 있습니다. */
    suspend fun joinFamily(role: Role, name: String, code: String, title: String = ""): Result<FamilyInfo>

    /** 앱 시작 시 저장된 가족이 있으면 동기화를 켭니다. */
    suspend fun resumeSync()

    suspend fun signOut()
    fun requestSync()
}
