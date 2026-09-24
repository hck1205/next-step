package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nextstep.app.data.local.entity.RoadmapItemEntity

@Dao
interface RoadmapDao : SyncDao<RoadmapItemEntity> {
    @Query("SELECT * FROM roadmap_items WHERE familyId = :familyId AND deleted = 0 ORDER BY status, targetDate, orderIndex")
    fun observeAll(familyId: String): Flow<List<RoadmapItemEntity>>

    @Query("SELECT * FROM roadmap_items WHERE id = :id")
    override suspend fun getById(id: String): RoadmapItemEntity?

    @Query("SELECT COUNT(*) FROM roadmap_items WHERE familyId = :familyId AND deleted = 0")
    suspend fun count(familyId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: RoadmapItemEntity)

    @Query("SELECT * FROM roadmap_items WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<RoadmapItemEntity>

    @Query("UPDATE roadmap_items SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
