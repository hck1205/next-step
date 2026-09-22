package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nextstep.app.data.local.entity.TopicEntity

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE familyId = :familyId AND deleted = 0 ORDER BY subjectId, orderIndex")
    fun observeAll(familyId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId AND deleted = 0 ORDER BY orderIndex")
    fun observeBySubject(subjectId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId AND deleted = 0 ORDER BY orderIndex")
    suspend fun getBySubject(subjectId: String): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getById(id: String): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TopicEntity>)

    @Query("SELECT * FROM topics WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<TopicEntity>

    @Query("UPDATE topics SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}
