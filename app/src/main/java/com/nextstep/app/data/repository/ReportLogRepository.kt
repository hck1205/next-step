package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.ReportLogEntity
import com.nextstep.app.domain.report.ReportKind
import kotlinx.coroutines.flow.Flow

/** 수업 리포트 보낸 기록. 보낸 사람은 현재 프로필로 채웁니다. */
interface ReportLogRepository {
    val logs: Flow<List<ReportLogEntity>>
    suspend fun record(kind: ReportKind, title: String, byName: String)
}
