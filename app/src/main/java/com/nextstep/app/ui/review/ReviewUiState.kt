package com.nextstep.app.ui.review

import com.nextstep.app.domain.stats.ReviewItem
import com.nextstep.app.domain.stats.ReviewReason

/** 배울 것 › 복습: 과목을 가로질러 지금 다시 볼 단원, 이유별로 묶음(급한 이유 먼저). */
data class ReviewUiState(
    val sections: List<Pair<ReviewReason, List<ReviewItem>>> = emptyList(),
    val total: Int = 0,
    /** 과목마다 남은 복습 단원 수(많은 과목 먼저). */
    val perSubject: List<Pair<String, Int>> = emptyList(),
    val loaded: Boolean = false,
)
