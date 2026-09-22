package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nextstep.app.data.local.entity.TaskEntity

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE familyId = :familyId AND deleted = 0 ORDER BY done, dueDate, updatedAt DESC")
    fun observeAll(familyId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TaskEntity>)

    @Query("SELECT * FROM tasks WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<TaskEntity>

    @Query("UPDATE tasks SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}
