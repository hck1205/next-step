package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.GoalStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalStepDao {
    @Query("SELECT * FROM goal_steps WHERE familyId = :familyId AND deleted = 0 ORDER BY orderIndex")
    fun observeAll(familyId: String): Flow<List<GoalStepEntity>>

    @Query("SELECT * FROM goal_steps WHERE id = :id")
    suspend fun getById(id: String): GoalStepEntity?

    @Query("SELECT * FROM goal_steps WHERE goalId = :goalId AND deleted = 0")
    suspend fun getByGoal(goalId: String): List<GoalStepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: GoalStepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<GoalStepEntity>)

    @Query("SELECT * FROM goal_steps WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<GoalStepEntity>

    @Query("UPDATE goal_steps SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}
