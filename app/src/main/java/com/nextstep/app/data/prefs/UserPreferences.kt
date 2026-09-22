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
import com.nextstep.app.data.local.entity.MemberEntity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nextstep_prefs")

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

/** 진행 중인 학습 타이머. 앱이 종료돼도 복원되도록 DataStore 에 저장합니다. */
data class RunningTimer(val subjectId: String?, val startedAt: Long)

class UserPreferences(private val context: Context) : UserPreferencesStore {
    private object Keys {
        val ROLE = stringPreferencesKey("role")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val FAMILY_ID = stringPreferencesKey("family_id")
        val PAIRING_CODE = stringPreferencesKey("pairing_code")
        val STUDENT_NAME = stringPreferencesKey("student_name")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val MEMBER_ID = stringPreferencesKey("member_id")
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
        )
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
            p[Keys.MEMBER_ID] = memberId
            p[Keys.ROLE] = role.name
            p[Keys.DISPLAY_NAME] = displayName
            p[Keys.FAMILY_ID] = familyId
            p[Keys.PAIRING_CODE] = pairingCode
            p[Keys.STUDENT_NAME] = studentName
            p[Keys.ONBOARDED] = true
        }
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
