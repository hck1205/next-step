package com.nextstep.app.data.repository.room

import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.data.local.dao.MemberDao
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class RoomMemberRepository(
    private val dao: MemberDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), MemberRepository {

    override val members: Flow<List<MemberEntity>> = scope.scopedList { dao.observeAll(it) }

    override val myMember: Flow<MemberEntity?> = scope.profile.map { it.memberId }.distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(null) else dao.observeById(id) }

    override suspend fun setSubjects(memberId: String, subjectIds: List<String>) =
        modify(memberId) { it.copy(subjectIds = subjectIds.joinToString(",")) }

    override suspend fun updateProfile(memberId: String, name: String, title: String) =
        modify(memberId) { it.copy(name = name, title = title) }

    override suspend fun setMentorEnabled(memberId: String, enabled: Boolean) =
        modify(memberId) { it.copy(mentorEnabled = if (it.isMentor) true else enabled) }

    override suspend fun setGradeYear(memberId: String, gradeYear: Int) =
        modify(memberId) { it.copy(gradeYear = gradeYear.coerceIn(0, GrowthStage.MAX_GRADE)) }

    override suspend fun setBirthDate(memberId: String, birthDate: java.time.LocalDate?) =
        modify(memberId) { it.copy(birthDate = birthDate?.toEpochDay()) }

    override suspend fun setUiLevel(memberId: String, level: StudentUiLevel?) =
        modify(memberId) { it.copy(uiLevel = level?.name.orEmpty()) }

    override suspend fun markUiLevelSeen(memberId: String, level: StudentUiLevel) {
        if (dao.getById(memberId)?.seenUiLevel == level.name) return
        modify(memberId) { it.copy(seenUiLevel = level.name) }
    }

    override suspend fun setSelfDirection(memberId: String, stage: SelfDirectionStage?) =
        modify(memberId) { it.copy(selfDirection = stage?.name.orEmpty()) }

    override suspend fun remove(memberId: String) = modify(memberId) { it.copy(deleted = true) }

    private suspend fun modify(id: String, change: (MemberEntity) -> MemberEntity) {
        val current = dao.getById(id) ?: return
        dao.upsert(change(current).copy(updatedAt = now(), dirty = true))
        pushLater()
    }
}
