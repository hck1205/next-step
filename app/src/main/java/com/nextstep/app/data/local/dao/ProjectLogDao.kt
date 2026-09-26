package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.ProjectLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectLogDao : SyncDao<ProjectLogEntity> {
    @Query("SELECT * FROM project_logs WHERE familyId = :familyId AND deleted = 0 ORDER BY date DESC")
    fun observeAll(familyId: String): Flow<List<ProjectLogEntity>>

    @Query("SELECT * FROM project_logs WHERE goalId = :goalId AND item = :item AND date = :date AND deleted = 0")
    suspend fun findOn(goalId: String, item: String, date: Long): List<ProjectLogEntity>

    @Query("SELECT * FROM project_logs WHERE id = :id")
    override suspend fun getById(id: String): ProjectLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: ProjectLogEntity)

    @Query("SELECT * FROM project_logs WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<ProjectLogEntity>

    @Query("UPDATE project_logs SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
