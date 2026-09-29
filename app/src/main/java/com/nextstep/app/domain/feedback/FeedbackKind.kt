package com.nextstep.app.domain.feedback

/**
 * 주간 피드백의 사실 종류. [good] 은 잘한 것(칭찬), 아니면 챙길 것. [familyOnly] 는 가족의 일이라 멘토에게 보이지 않습니다.
 * 문구는 역할마다 따로([StudentVoice]·[ParentVoice]·[MentorVoice]), 사실은 한 벌입니다.
 */
enum class FeedbackKind(val good: Boolean, val familyOnly: Boolean = false) {
    STUDY_DAYS_UP(good = true),
    STUDY_STEADY(good = true),
    STUDY_DAYS_DOWN(good = false),
    TASKS_WELL(good = true),
    TASKS_OVERDUE(good = false),
    SELF_MADE_UP(good = true, familyOnly = true),
    SCORE_UP(good = true),
    SCORE_DOWN(good = false),
    SUBJECT_GAP(good = false),
    REVIEW_BACKLOG(good = false),
}
