package com.nextstep.app.ui.records

import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.ObservationEntity

/** Records 화면의 사용자 의도. 세그먼트 전환은 내비게이션 인자로 다룹니다. */
sealed interface RecordsEvent {
    data class SaveGrowth(val record: GrowthRecordEntity) : RecordsEvent
    data class DeleteGrowth(val id: String) : RecordsEvent
    data class AddObservation(val observation: ObservationEntity) : RecordsEvent
    data class DeleteObservation(val id: String) : RecordsEvent
}
