package com.nextstep.app.data.repository.room

import com.nextstep.app.data.prefs.UserPreferencesStore
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.repository.FamilyScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PrefsFamilyScope(private val prefs: UserPreferencesStore) : FamilyScope {
    override val profile: Flow<UserProfile> = prefs.profile
    override val familyId: Flow<String?> = profile.map { it.familyId }.distinctUntilChanged()
    override suspend fun currentProfile(): UserProfile = profile.first()
    override suspend fun requireFamilyId(): String = currentProfile().familyId ?: error("가족 정보가 설정되지 않았습니다")
}
