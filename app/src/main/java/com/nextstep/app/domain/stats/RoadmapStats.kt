package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.model.RoadmapStatus
import java.time.LocalDate

/** 로드맵 항목을 상태별로 셉니다. 기한 지남 = 완료가 아니고 목표일이 오늘 이전. */
object RoadmapStats {
    fun summarize(items: List<RoadmapItemEntity>, today: LocalDate): RoadmapSummary {
        val day = today.toEpochDay()
        return RoadmapSummary(
            total = items.size,
            inProgress = items.count { it.status == RoadmapStatus.IN_PROGRESS },
            done = items.count { it.status == RoadmapStatus.DONE },
            overdue = items.count { it.status != RoadmapStatus.DONE && (it.targetDate ?: Long.MAX_VALUE) < day },
        )
    }
}
