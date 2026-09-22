package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.NoteDao
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.NoteRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomNoteRepository(
    private val dao: NoteDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), NoteRepository {

    override val notes: Flow<List<NoteEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun add(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val profile = scope.currentProfile()
        dao.upsert(
            NoteEntity(
                familyId = scope.requireFamilyId(), authorRole = profile.role?.name ?: Role.STUDENT.name,
                authorName = profile.displayName, text = clean, createdAt = now(), updatedAt = now(),
            ),
        )
        pushLater()
    }

    override suspend fun delete(id: String) {
        val note = dao.getById(id) ?: return
        dao.upsert(note.copy(deleted = true, updatedAt = now(), dirty = true))
        pushLater()
    }
}
