package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.ReportLogDao
import com.nextstep.app.data.local.entity.ReportLogEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.ReportLogRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import com.nextstep.app.domain.report.ReportKind
import kotlinx.coroutines.flow.Flow

class RoomReportLogRepository(
    private val dao: ReportLogDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), ReportLogRepository {

    override val logs: Flow<List<ReportLogEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun record(kind: ReportKind, title: String, byName: String) {
        val now = now()
        dao.upsert(ReportLogEntity(familyId = familyIdOr(""), kind = kind.name, title = title, sentById = scope.currentProfile().memberId.orEmpty(), sentByName = byName, sentAt = now, updatedAt = now))
        pushLater()
    }
}
