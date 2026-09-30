package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.RewardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao : SyncDao<RewardEntity> {
    @Query("SELECT * FROM rewards WHERE familyId = :familyId AND deleted = 0 ORDER BY createdAt DESC")
    fun observeAll(familyId: String): Flow<List<RewardEntity>>

    @Query("SELECT * FROM rewards WHERE familyId = :familyId AND kind = :kind AND targetId = :targetId AND givenAt IS NULL AND deleted = 0")
    suspend fun findOpen(familyId: String, kind: String, targetId: String): List<RewardEntity>

    @Query("SELECT * FROM rewards WHERE id = :id")
    override suspend fun getById(id: String): RewardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: RewardEntity)

    @Query("SELECT * FROM rewards WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<RewardEntity>

    @Query("UPDATE rewards SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
