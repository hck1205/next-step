package com.nextstep.app.ui.growth

import com.nextstep.app.data.local.entity.GrowthRecordEntity

/** 신체 섹션의 사용자 의도. */
sealed interface GrowthEvent {
    data class Save(val record: GrowthRecordEntity) : GrowthEvent
    data class Delete(val id: String) : GrowthEvent
}
