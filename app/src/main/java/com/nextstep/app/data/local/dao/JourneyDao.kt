package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.JourneyItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {
    @Query("SELECT * FROM journey_items WHERE familyId = :familyId AND deleted = 0 ORDER BY dueDate")
    fun observeAll(familyId: String): Flow<List<JourneyItemEntity>>

    @Query("SELECT * FROM journey_items WHERE id = :id")
    suspend fun getById(id: String): JourneyItemEntity?

    @Query("SELECT * FROM journey_items WHERE familyId = :familyId AND templateId = :templateId AND deleted = 0 LIMIT 1")
    suspend fun getByTemplate(familyId: String, templateId: String): JourneyItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: JourneyItemEntity)

    @Query("SELECT * FROM journey_items WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<JourneyItemEntity>

    @Query("UPDATE journey_items SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}
