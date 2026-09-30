package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.FamilyEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyEventDao : SyncDao<FamilyEventEntity> {
    @Query("SELECT * FROM family_events WHERE familyId = :familyId AND deleted = 0 ORDER BY startDate, startMinute")
    fun observeAll(familyId: String): Flow<List<FamilyEventEntity>>

    @Query("SELECT * FROM family_events WHERE id = :id")
    override suspend fun getById(id: String): FamilyEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: FamilyEventEntity)

    @Query("SELECT * FROM family_events WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<FamilyEventEntity>

    @Query("UPDATE family_events SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
