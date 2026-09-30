package com.nextstep.app.ui.overview

import com.nextstep.app.domain.hub.Concern

/** 한눈에 밖으로 나가는 콜백: 타일을 누르면 그 관심사 탭으로 갑니다. */
data class OverviewActions(
    val onOpenConcern: (Concern) -> Unit = { _ -> },
)
