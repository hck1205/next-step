package com.nextstep.app.ui.talent

import com.nextstep.app.data.local.entity.ObservationEntity

/** 재능 섹션의 사용자 의도. */
sealed interface TalentEvent {
    data class Observe(val observation: ObservationEntity) : TalentEvent
    data class Delete(val id: String) : TalentEvent
}
