package com.nextstep.app.testing

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.repository.FamilyScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFamilyScope(role: Role = Role.STUDENT, familyId: String? = Fixtures.FAMILY, displayName: String = "테스터") : FamilyScope {
    val profileState = MutableStateFlow(UserProfile(role, displayName, familyId, "ABC123", "학생", onboarded = familyId != null, memberId = "me"))
    override val profile: Flow<UserProfile> = profileState
    override val familyId: Flow<String?> = profileState.map { it.familyId }
    override suspend fun currentProfile(): UserProfile = profileState.value
    override suspend fun requireFamilyId(): String = profileState.value.familyId ?: error("가족 정보가 설정되지 않았습니다")
}
