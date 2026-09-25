package com.nextstep.app.ui.kidme

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.domain.stats.StickerBoard
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/** 아이용 "나": 스티커판, 이번 주 끝낸 할 일, 최근에 한 것. */
data class KidMeUiState(
    val studentName: String = "",
    val today: LocalDate = DateUtils.today(),
    val board: StickerBoard? = null,
    val recentActivities: List<ActivityEntity> = emptyList(),
    val loaded: Boolean = false,
)
