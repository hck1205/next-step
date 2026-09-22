package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.MemberDao
import com.nextstep.app.data.local.dao.SubjectDao
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.newId
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.prefs.UserPreferencesStore
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.data.repository.OnboardingRepository
import com.nextstep.app.data.sync.FamilyInfo
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import java.util.Locale
import kotlin.random.Random

class RoomOnboardingRepository(
    private val prefs: UserPreferencesStore,
    private val memberDao: MemberDao,
    private val subjectDao: SubjectDao,
    private val sync: SyncManager,
    private val random: Random = Random.Default,
) : OnboardingRepository {

    override val profile: Flow<UserProfile> = prefs.profile
    override val syncStatus: StateFlow<SyncStatus> get() = sync.status
    override val syncAvailable: Boolean get() = sync.isAvailable

    override suspend fun createFamilyAsStudent(studentName: String): Result<FamilyInfo> {
        val info = FamilyInfo(familyId = newId(), pairingCode = generatePairingCode(), studentName = studentName)
        sync.createFamily(info).onFailure { return Result.failure(it) }
        val member = MemberEntity(familyId = info.familyId, role = Role.STUDENT.name, name = studentName)
        memberDao.upsert(member)
        prefs.completeOnboarding(Role.STUDENT, studentName, info.familyId, info.pairingCode, studentName, member.id)
        seedDefaultSubjects(info.familyId)
        sync.start(info.familyId)
        return Result.success(info)
    }

    override suspend fun joinFamily(role: Role, name: String, code: String, title: String): Result<FamilyInfo> {
        require(role != Role.STUDENT) { "학생은 코드로 참여할 수 없습니다" }
        if (!sync.isAvailable) return Result.failure(IllegalStateException(NO_SYNC_MESSAGE))
        val info = sync.findFamilyByCode(code.trim().uppercase(Locale.ROOT))
            .getOrElse { return Result.failure(it) }
            ?: return Result.failure(IllegalArgumentException("코드에 해당하는 학생을 찾지 못했습니다"))
        val member = MemberEntity(familyId = info.familyId, role = role.name, name = name, title = title, mentorEnabled = role == Role.MENTOR)
        memberDao.upsert(member)
        prefs.completeOnboarding(role, name, info.familyId, info.pairingCode, info.studentName, member.id)
        sync.start(info.familyId)
        return Result.success(info)
    }

    override suspend fun resumeSync() {
        prefs.profile.first().familyId?.let { sync.start(it) }
    }

    override suspend fun signOut() {
        sync.stop()
        prefs.reset()
    }

    override fun requestSync() = sync.requestPush()

    private fun generatePairingCode(): String =
        (1..PAIRING_CODE_LENGTH).map { PAIRING_ALPHABET[random.nextInt(PAIRING_ALPHABET.length)] }.joinToString("")

    private suspend fun seedDefaultSubjects(familyId: String) {
        if (subjectDao.count(familyId) > 0) return
        subjectDao.upsertAll(
            DEFAULT_SUBJECTS.mapIndexed { i, (name, color) ->
                SubjectEntity(familyId = familyId, name = name, color = color, orderIndex = i, weeklyGoalMinutes = DEFAULT_WEEKLY_GOAL_MINUTES)
            },
        )
        sync.requestPush()
    }

    companion object {
        /** 헷갈리는 글자(0/O, 1/I)를 뺀 알파벳. */
        private const val PAIRING_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        private const val PAIRING_CODE_LENGTH = 6
        private const val DEFAULT_WEEKLY_GOAL_MINUTES = 180
        private const val NO_SYNC_MESSAGE = "동기화 서버가 설정되지 않아 학생 기기와 연결할 수 없습니다. README 의 Firebase 설정을 참고하세요."
        private val DEFAULT_SUBJECTS = listOf(
            "국어" to 0xFFEF4444, "수학" to 0xFF3B82F6, "영어" to 0xFF10B981, "과학" to 0xFF8B5CF6, "사회" to 0xFFF59E0B,
        )
    }
}
