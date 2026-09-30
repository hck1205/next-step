package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao : SyncDao<ActivityEntity> {
    @Query("SELECT * FROM activities WHERE familyId = :familyId AND deleted = 0 ORDER BY date DESC")
    fun observeAll(familyId: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    override suspend fun getById(id: String): ActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: ActivityEntity)

    @Query("SELECT * FROM activities WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<ActivityEntity>

    @Query("UPDATE activities SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
