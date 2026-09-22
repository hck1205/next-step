package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.StudySessionDao
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.prefs.RunningTimer
import com.nextstep.app.data.prefs.UserPreferences
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.StudySessionRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RoomStudySessionRepository(
    private val dao: StudySessionDao,
    private val prefs: UserPreferences,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), StudySessionRepository {

    override val sessions: Flow<List<StudySessionEntity>> = scope.scopedList { dao.observeAll(it) }
    override val runningTimer: Flow<RunningTimer?> = prefs.runningTimer

    override suspend fun save(session: StudySessionEntity) {
        dao.upsert(session.copy(familyId = familyIdOr(session.familyId), updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun delete(id: String) {
        val session = dao.getById(id) ?: return
        save(session.copy(deleted = true))
    }

    override suspend fun startTimer(subjectId: String?) = prefs.startTimer(subjectId, now())

    override suspend fun stopTimer(note: String): StudySessionEntity? {
        val timer = prefs.runningTimer.first() ?: return null
        prefs.clearTimer()
        val end = now()
        val minutes = ((end - timer.startedAt) / MILLIS_PER_MINUTE).toInt()
        if (minutes < MIN_RECORDED_MINUTES) return null
        val session = StudySessionEntity(
            familyId = scope.requireFamilyId(), subjectId = timer.subjectId, startAt = timer.startedAt, endAt = end,
            durationMinutes = minutes, note = note, fromTimer = true,
        )
        save(session)
        return session
    }

    override suspend fun cancelTimer() = prefs.clearTimer()

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
        const val MIN_RECORDED_MINUTES = 1
    }
}
