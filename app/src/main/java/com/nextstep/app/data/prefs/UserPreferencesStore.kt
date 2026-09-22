package com.nextstep.app.data.prefs

import com.nextstep.app.data.model.Role
import kotlinx.coroutines.flow.Flow

/** 기기 로컬 설정(프로필, 진행 중 타이머)의 경계. 구현은 DataStore, 테스트는 메모리. */
interface UserPreferencesStore {
    val profile: Flow<UserProfile>
    val runningTimer: Flow<RunningTimer?>
    suspend fun completeOnboarding(role: Role, displayName: String, familyId: String, pairingCode: String, studentName: String, memberId: String)
    suspend fun updateStudentName(name: String)
    suspend fun startTimer(subjectId: String?, startedAt: Long)
    suspend fun clearTimer()
    suspend fun reset()
}
