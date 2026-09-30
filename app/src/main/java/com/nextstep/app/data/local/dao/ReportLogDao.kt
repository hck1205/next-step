package com.nextstep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextstep.app.data.local.entity.ReportLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportLogDao : SyncDao<ReportLogEntity> {
    @Query("SELECT * FROM report_logs WHERE familyId = :familyId AND deleted = 0 ORDER BY sentAt DESC")
    fun observeAll(familyId: String): Flow<List<ReportLogEntity>>

    @Query("SELECT * FROM report_logs WHERE id = :id")
    override suspend fun getById(id: String): ReportLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(item: ReportLogEntity)

    @Query("SELECT * FROM report_logs WHERE familyId = :familyId AND dirty = 1")
    override suspend fun getDirty(familyId: String): List<ReportLogEntity>

    @Query("UPDATE report_logs SET dirty = 0 WHERE id IN (:ids)")
    override suspend fun markClean(ids: List<String>)
}
