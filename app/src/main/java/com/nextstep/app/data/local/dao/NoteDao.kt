package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nextstep.app.data.local.entity.NoteEntity

@Dao
interface NoteDao : SyncDao<NoteEntity> {
    @Query("SELECT * FROM notes WHERE familyId = :familyId AND deleted = 0 ORDER BY createdAt DESC")
    fun observeAll(familyId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    override suspend fun getById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<NoteEntity>)

    @Query("SELECT * FROM notes WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<NoteEntity>

    @Query("UPDATE notes SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
