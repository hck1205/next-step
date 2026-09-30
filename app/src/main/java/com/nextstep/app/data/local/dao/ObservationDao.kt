package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.ObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao : SyncDao<ObservationEntity> {
    @Query("SELECT * FROM observations WHERE familyId = :familyId AND deleted = 0 ORDER BY date DESC")
    fun observeAll(familyId: String): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE id = :id")
    override suspend fun getById(id: String): ObservationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: ObservationEntity)

    @Query("SELECT * FROM observations WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<ObservationEntity>

    @Query("UPDATE observations SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
