package com.nextstep.app.domain.cheer

/** 오늘 자녀가 한 일의 요약. 칭찬 문구의 근거가 됩니다. */
data class CheerSnapshot(
    val todayMinutes: Int = 0,
    val streak: Int = 0,
    val doneToday: Int = 0,
    val reviewedToday: Int = 0,
    /** 오늘 가장 오래 공부한 과목 이름. 오늘 기록이 없으면 null. */
    val topSubjectName: String? = null,
)
