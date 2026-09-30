package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthRecordDao : SyncDao<GrowthRecordEntity> {
    @Query("SELECT * FROM growth_records WHERE familyId = :familyId AND deleted = 0 ORDER BY date")
    fun observeAll(familyId: String): Flow<List<GrowthRecordEntity>>

    @Query("SELECT * FROM growth_records WHERE id = :id")
    override suspend fun getById(id: String): GrowthRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: GrowthRecordEntity)

    @Query("SELECT * FROM growth_records WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<GrowthRecordEntity>

    @Query("UPDATE growth_records SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
