package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nextstep.app.data.local.entity.GradeEntity

@Dao
interface GradeDao : SyncDao<GradeEntity> {
    @Query("SELECT * FROM grades WHERE familyId = :familyId AND deleted = 0 ORDER BY date DESC, updatedAt DESC")
    fun observeAll(familyId: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE id = :id")
    override suspend fun getById(id: String): GradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: GradeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<GradeEntity>)

    @Query("SELECT * FROM grades WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<GradeEntity>

    @Query("UPDATE grades SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
