package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.WeekPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekPlanDao : SyncDao<WeekPlanEntity> {
    @Query("SELECT * FROM week_plans WHERE familyId = :familyId AND deleted = 0 ORDER BY weekStart DESC")
    fun observeAll(familyId: String): Flow<List<WeekPlanEntity>>

    @Query("SELECT * FROM week_plans WHERE familyId = :familyId AND weekStart = :weekStart AND deleted = 0 LIMIT 1")
    suspend fun findByWeek(familyId: String, weekStart: Long): WeekPlanEntity?

    @Query("SELECT * FROM week_plans WHERE id = :id")
    override suspend fun getById(id: String): WeekPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: WeekPlanEntity)

    @Query("SELECT * FROM week_plans WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<WeekPlanEntity>

    @Query("UPDATE week_plans SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
