package com.nextstep.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nextstep.app.data.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nextstep_prefs")

class UserPreferences(private val context: Context) : UserPreferencesStore {
    private object Keys {
        val ROLE = stringPreferencesKey("role")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val FAMILY_ID = stringPreferencesKey("family_id")
        val PAIRING_CODE = stringPreferencesKey("pairing_code")
        val STUDENT_NAME = stringPreferencesKey("student_name")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val MEMBER_ID = stringPreferencesKey("member_id")
        val CHILDREN = stringPreferencesKey("linked_children")
        val TIMER_SUBJECT = stringPreferencesKey("timer_subject")
        val TIMER_STARTED_AT = longPreferencesKey("timer_started_at")
    }

    override val profile: Flow<UserProfile> = context.dataStore.data.map { p ->
        UserProfile(
            role = Role.from(p[Keys.ROLE]),
            displayName = p[Keys.DISPLAY_NAME] ?: "",
            familyId = p[Keys.FAMILY_ID],
            pairingCode = p[Keys.PAIRING_CODE],
            studentName = p[Keys.STUDENT_NAME] ?: "",
            onboarded = p[Keys.ONBOARDED] ?: false,
            memberId = p[Keys.MEMBER_ID],
            children = linkedChildren(p),
        )
    }

    /** 저장된 자녀 목록. 다자녀 기능 이전에 온보딩한 기기는 지금 가족 하나로 채웁니다. */
    private fun linkedChildren(p: Preferences): List<LinkedChild> = LinkedChildCodec.decode(p[Keys.CHILDREN]).ifEmpty {
        val familyId = p[Keys.FAMILY_ID] ?: return@ifEmpty emptyList()
        listOf(LinkedChild(familyId, p[Keys.STUDENT_NAME] ?: "", p[Keys.PAIRING_CODE] ?: "", p[Keys.MEMBER_ID] ?: ""))
    }

    override val runningTimer: Flow<RunningTimer?> = context.dataStore.data.map { p ->
        val started = p[Keys.TIMER_STARTED_AT] ?: return@map null
        RunningTimer(subjectId = p[Keys.TIMER_SUBJECT], startedAt = started)
    }

    override suspend fun completeOnboarding(
        role: Role,
        displayName: String,
        familyId: String,
        pairingCode: String,
        studentName: String,
        memberId: String,
    ) {
        context.dataStore.edit { p ->
            // 기존 목록(다자녀 이전 기기면 지금 가족 하나)을 먼저 읽어야 새 자녀로 바뀌기 전 가족을 잃지 않습니다.
            val before = linkedChildren(p)
            p[Keys.CHILDREN] = LinkedChildCodec.encode(LinkedChildCodec.upsert(before, LinkedChild(familyId, studentName, pairingCode, memberId)))
            p[Keys.MEMBER_ID] = memberId
            p[Keys.ROLE] = role.name
            p[Keys.DISPLAY_NAME] = displayName
            p[Keys.FAMILY_ID] = familyId
            p[Keys.PAIRING_CODE] = pairingCode
            p[Keys.STUDENT_NAME] = studentName
            p[Keys.ONBOARDED] = true
        }
    }

    override suspend fun switchChild(familyId: String): Boolean {
        var switched = false
        context.dataStore.edit { p ->
            val child = linkedChildren(p).firstOrNull { it.familyId == familyId } ?: return@edit
            p[Keys.FAMILY_ID] = child.familyId
            p[Keys.PAIRING_CODE] = child.pairingCode
            p[Keys.STUDENT_NAME] = child.studentName
            p[Keys.MEMBER_ID] = child.memberId
            switched = true
        }
        return switched
    }

    override suspend fun updateStudentName(name: String) {
        context.dataStore.edit { it[Keys.STUDENT_NAME] = name }
    }

    override suspend fun startTimer(subjectId: String?, startedAt: Long) {
        context.dataStore.edit { p ->
            if (subjectId != null) p[Keys.TIMER_SUBJECT] = subjectId else p.remove(Keys.TIMER_SUBJECT)
            p[Keys.TIMER_STARTED_AT] = startedAt
        }
    }

    override suspend fun clearTimer() {
        context.dataStore.edit { p ->
            p.remove(Keys.TIMER_SUBJECT)
            p.remove(Keys.TIMER_STARTED_AT)
        }
    }

    override suspend fun reset() {
        context.dataStore.edit { it.clear() }
    }
}
