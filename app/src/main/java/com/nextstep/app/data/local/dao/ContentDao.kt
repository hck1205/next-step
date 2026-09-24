package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nextstep.app.data.local.entity.ContentEntity

@Dao
interface ContentDao : SyncDao<ContentEntity> {
    /** 이 가족이 등록한 콘텐츠 + 공용 저장소(GLOBAL) 콘텐츠. */
    @Query("SELECT * FROM contents WHERE (familyId = :familyId OR scope = 'GLOBAL') AND deleted = 0 ORDER BY createdAt DESC")
    fun observeAll(familyId: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM contents WHERE id = :id")
    override suspend fun getById(id: String): ContentEntity?

    @Query("SELECT * FROM contents WHERE (familyId = :familyId OR scope = 'GLOBAL') AND videoId = :videoId AND deleted = 0 LIMIT 1")
    suspend fun findByVideoId(familyId: String, videoId: String): ContentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: ContentEntity)

    @Query("SELECT * FROM contents WHERE familyId = :familyId AND scope = 'FAMILY' AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<ContentEntity>

    @Query("UPDATE contents SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
