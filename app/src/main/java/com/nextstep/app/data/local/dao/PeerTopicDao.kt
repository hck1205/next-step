package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.PeerTopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerTopicDao {
    @Query("SELECT * FROM peer_topics WHERE deleted = 0 ORDER BY families DESC")
    fun observeAll(): Flow<List<PeerTopicEntity>>

    @Query("SELECT * FROM peer_topics WHERE id = :id")
    suspend fun getById(id: String): PeerTopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PeerTopicEntity)
}
