package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.CheerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheerDao : SyncDao<CheerEntity> {
    @Query("SELECT * FROM cheers WHERE familyId = :familyId AND deleted = 0 ORDER BY createdAt DESC")
    fun observeAll(familyId: String): Flow<List<CheerEntity>>

    @Query("SELECT * FROM cheers WHERE familyId = :familyId AND taskId = :taskId AND fromId = :fromId AND deleted = 0")
    suspend fun findMine(familyId: String, taskId: String, fromId: String): List<CheerEntity>

    @Query("SELECT * FROM cheers WHERE id = :id")
    override suspend fun getById(id: String): CheerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: CheerEntity)

    @Query("SELECT * FROM cheers WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<CheerEntity>

    @Query("UPDATE cheers SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
