package com.nextstep.app.domain.feedback

/** 한 사람에게 보이는 피드백 한 줄: 사실([title])과 그 사람이 할 수 있는 한 걸음([detail]). [good] 이면 잘한 것. */
data class FeedbackLine(val title: String, val detail: String, val good: Boolean)
