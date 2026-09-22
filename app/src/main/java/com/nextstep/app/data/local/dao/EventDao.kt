package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nextstep.app.data.local.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE familyId = :familyId AND deleted = 0 ORDER BY startAt")
    fun observeAll(familyId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<EventEntity>)

    @Query("SELECT * FROM events WHERE familyId = :familyId AND dirty = 1")
    suspend fun getDirty(familyId: String): List<EventEntity>

    @Query("UPDATE events SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)
}
