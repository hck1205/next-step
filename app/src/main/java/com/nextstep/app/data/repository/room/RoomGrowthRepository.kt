package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.GrowthRecordDao
import com.nextstep.app.data.local.dao.ObservationDao
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.GrowthRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomGrowthRepository(
    private val recordDao: GrowthRecordDao,
    private val observationDao: ObservationDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), GrowthRepository {

    override val records: Flow<List<GrowthRecordEntity>> = scope.scopedList { recordDao.observeAll(it) }
    override val observations: Flow<List<ObservationEntity>> = scope.scopedList { observationDao.observeAll(it) }

    override suspend fun saveRecord(record: GrowthRecordEntity) {
        if (record.isEmpty) return
        val existing = recordDao.getById(record.id)
        val author = existing?.createdByRole?.ifEmpty { null } ?: scope.currentProfile().role?.name ?: ""
        recordDao.upsert(
            record.copy(
                familyId = familyIdOr(record.familyId), heightCm = record.heightCm?.takeIf { it > 0 }, weightKg = record.weightKg?.takeIf { it > 0 },
                visionLeft = record.visionLeft?.coerceIn(0.0, MAX_VISION), visionRight = record.visionRight?.coerceIn(0.0, MAX_VISION),
                note = record.note.trim(), createdByRole = author, updatedAt = now(), dirty = true,
            ),
        )
        pushLater()
    }

    override suspend fun deleteRecord(id: String) {
        val item = recordDao.getById(id) ?: return
        recordDao.upsert(item.copy(deleted = true, updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun addObservation(observation: ObservationEntity) {
        val text = observation.text.trim()
        if (text.isEmpty()) return
        val profile = scope.currentProfile()
        observationDao.upsert(
            observation.copy(
                familyId = familyIdOr(observation.familyId), text = text, strength = observation.strength.coerceIn(1, MAX_STRENGTH),
                authorRole = profile.role?.name ?: "", authorName = profile.displayName, updatedAt = now(), dirty = true,
            ),
        )
        pushLater()
    }

    override suspend fun deleteObservation(id: String) {
        val item = observationDao.getById(id) ?: return
        observationDao.upsert(item.copy(deleted = true, updatedAt = now(), dirty = true))
        pushLater()
    }

    private companion object {
        const val MAX_VISION = 2.0
        const val MAX_STRENGTH = 3
    }
}
