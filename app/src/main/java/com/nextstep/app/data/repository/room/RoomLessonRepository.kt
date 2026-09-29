package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.LessonDao
import com.nextstep.app.data.local.entity.LessonEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.LessonRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import com.nextstep.app.domain.lesson.LessonStatus
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class RoomLessonRepository(
    private val dao: LessonDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), LessonRepository {

    override val lessons: Flow<List<LessonEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun mark(date: LocalDate, status: LessonStatus?, note: String) {
        val familyId = familyIdOr("")
        val mentorId = scope.currentProfile().memberId.orEmpty()
        val old = dao.find(familyId, mentorId, date.toEpochDay()).firstOrNull()
        val now = now()
        when {
            status == null -> old?.let { dao.upsert(it.copy(deleted = true, updatedAt = now, dirty = true)) }
            old != null -> dao.upsert(old.copy(status = status.name, note = note.trim(), updatedAt = now, dirty = true))
            else -> dao.upsert(LessonEntity(familyId = familyId, mentorId = mentorId, date = date.toEpochDay(), status = status.name, note = note.trim(), updatedAt = now))
        }
        pushLater()
    }
}
