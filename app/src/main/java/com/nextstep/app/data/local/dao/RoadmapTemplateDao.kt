package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.RoadmapTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoadmapTemplateDao {
    @Query("SELECT * FROM roadmap_templates ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RoadmapTemplateEntity>>

    @Query("SELECT * FROM roadmap_templates WHERE id = :id")
    suspend fun getById(id: String): RoadmapTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RoadmapTemplateEntity)

    @Query("DELETE FROM roadmap_templates WHERE id = :id")
    suspend fun delete(id: String)
}
