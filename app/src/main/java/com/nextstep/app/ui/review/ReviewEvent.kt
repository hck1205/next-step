package com.nextstep.app.ui.review

import com.nextstep.app.domain.stats.ReviewItem

/** 복습 화면의 사용자 의도. */
sealed interface ReviewEvent {
    /** 이 단원을 오늘 할 일로(작성자 역할은 ViewModel 이 채움). */
    data class AddTask(val item: ReviewItem) : ReviewEvent
    /** 복습(예습이면 예습)을 했다고 표시. 학생 본인만. */
    data class MarkDone(val item: ReviewItem) : ReviewEvent
}
