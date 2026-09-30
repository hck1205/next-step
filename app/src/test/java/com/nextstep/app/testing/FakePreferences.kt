package com.nextstep.app.testing

import com.nextstep.app.data.model.Role
import com.nextstep.app.data.prefs.LinkedChild
import com.nextstep.app.data.prefs.LinkedChildCodec
import com.nextstep.app.data.prefs.RunningTimer
import com.nextstep.app.data.prefs.UserPreferencesStore
import com.nextstep.app.data.prefs.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferences(initial: UserProfile = UserProfile(null, "", null, null, "", onboarded = false, memberId = null)) : UserPreferencesStore {
    override val profile = MutableStateFlow(initial)
    override val runningTimer = MutableStateFlow<RunningTimer?>(null)
    var resets = 0

    override suspend fun completeOnboarding(role: Role, displayName: String, familyId: String, pairingCode: String, studentName: String, memberId: String) {
        val children = LinkedChildCodec.upsert(profile.value.children, LinkedChild(familyId, studentName, pairingCode, memberId))
        profile.value = UserProfile(role, displayName, familyId, pairingCode, studentName, onboarded = true, memberId = memberId, children = children)
    }

    override suspend fun switchChild(familyId: String): Boolean {
        val child = profile.value.children.firstOrNull { it.familyId == familyId } ?: return false
        profile.value = profile.value.copy(familyId = child.familyId, pairingCode = child.pairingCode, studentName = child.studentName, memberId = child.memberId)
        return true
    }

    override suspend fun updateStudentName(name: String) { profile.value = profile.value.copy(studentName = name) }
    override suspend fun startTimer(subjectId: String?, startedAt: Long) { runningTimer.value = RunningTimer(subjectId, startedAt) }
    override suspend fun clearTimer() { runningTimer.value = null }
    override suspend fun reset() { resets++; profile.value = UserProfile(null, "", null, null, "", onboarded = false, memberId = null); runningTimer.value = null }
}
