package com.nextstep.app.ui.feedback

import com.nextstep.app.domain.mentor.FeedbackWeek

/** 과제·피드백 › 피드백: 주마다 묶은 메모·피드백(최신 주 먼저)과 이번 주 요약. */
data class FeedbackUiState(
    val weeks: List<FeedbackWeek> = emptyList(),
    val thisWeekCount: Int = 0,
    val fromMentors: Int = 0,
    val fromFamily: Int = 0,
    val loaded: Boolean = false,
)
