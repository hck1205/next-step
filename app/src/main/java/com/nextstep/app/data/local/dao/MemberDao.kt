package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nextstep.app.data.local.entity.MemberEntity

@Dao
interface MemberDao : SyncDao<MemberEntity> {
    @Query("SELECT * FROM members WHERE familyId = :familyId AND deleted = 0 ORDER BY joinedAt")
    fun observeAll(familyId: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id")
    override suspend fun getById(id: String): MemberEntity?

    @Query("SELECT * FROM members WHERE id = :id")
    fun observeById(id: String): Flow<MemberEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: MemberEntity)

    @Query("SELECT * FROM members WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<MemberEntity>

    @Query("UPDATE members SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
