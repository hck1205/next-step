package com.nextstep.app.domain.mentor

import com.nextstep.app.data.local.entity.NoteEntity

/** 피드백을 주(週)로 묶은 한 덩어리. [label] 은 "이번 주", "지난주", "3주 전" 처럼 지난 정도로 씁니다. */
data class FeedbackWeek(val label: String, val notes: List<NoteEntity>)
