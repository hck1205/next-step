package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nextstep.app.data.local.entity.StudySessionEntity

@Dao
interface StudySessionDao : SyncDao<StudySessionEntity> {
    @Query("SELECT * FROM study_sessions WHERE familyId = :familyId AND deleted = 0 ORDER BY startAt DESC")
    fun observeAll(familyId: String): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE familyId = :familyId AND deleted = 0 AND startAt >= :fromMillis ORDER BY startAt DESC")
    fun observeSince(familyId: String, fromMillis: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE id = :id")
    override suspend fun getById(id: String): StudySessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: StudySessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StudySessionEntity>)

    @Query("SELECT * FROM study_sessions WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<StudySessionEntity>

    @Query("UPDATE study_sessions SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
