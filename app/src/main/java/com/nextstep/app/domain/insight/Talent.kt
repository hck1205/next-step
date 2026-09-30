package com.nextstep.app.domain.insight

/** 학부모 화면의 "재능 발견" 카드. strength 는 0~1 로 신호의 확신도. */
data class Talent(val title: String, val body: String, val subjectId: String?, val strength: Float)
