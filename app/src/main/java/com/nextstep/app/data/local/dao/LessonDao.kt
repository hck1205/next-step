package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao : SyncDao<LessonEntity> {
    @Query("SELECT * FROM lessons WHERE familyId = :familyId AND deleted = 0 ORDER BY date")
    fun observeAll(familyId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE familyId = :familyId AND mentorId = :mentorId AND date = :date AND deleted = 0")
    suspend fun find(familyId: String, mentorId: String, date: Long): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE id = :id")
    override suspend fun getById(id: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: LessonEntity)

    @Query("SELECT * FROM lessons WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<LessonEntity>

    @Query("UPDATE lessons SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
