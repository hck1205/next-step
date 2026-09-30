package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao : SyncDao<GoalEntity> {
    @Query("SELECT * FROM goals WHERE familyId = :familyId AND deleted = 0 ORDER BY createdAt")
    fun observeAll(familyId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    override suspend fun getById(id: String): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: GoalEntity)

    @Query("SELECT * FROM goals WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<GoalEntity>

    @Query("UPDATE goals SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
