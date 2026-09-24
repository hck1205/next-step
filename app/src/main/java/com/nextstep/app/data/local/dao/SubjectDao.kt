package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nextstep.app.data.local.entity.SubjectEntity

@Dao
interface SubjectDao : SyncDao<SubjectEntity> {
    @Query("SELECT * FROM subjects WHERE familyId = :familyId AND deleted = 0 ORDER BY orderIndex, name")
    fun observeAll(familyId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE familyId = :familyId AND deleted = 0 ORDER BY orderIndex, name")
    suspend fun getAll(familyId: String): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :id")
    override suspend fun getById(id: String): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun observeById(id: String): Flow<SubjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SubjectEntity>)

    @Query("SELECT * FROM subjects WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<SubjectEntity>

    @Query("UPDATE subjects SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)

    @Query("SELECT COUNT(*) FROM subjects WHERE familyId = :familyId AND deleted = 0")
    suspend fun count(familyId: String): Int
}
