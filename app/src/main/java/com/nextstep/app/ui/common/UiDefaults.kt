package com.nextstep.app.ui.common

/** 화면 공통 상수. 화면마다 흩어져 있던 숫자를 한 곳에서 관리합니다. */
object UiDefaults {
    /** 구독자가 사라진 뒤 상태 흐름을 유지하는 시간. 화면 회전 동안 재계산을 막습니다. */
    const val STATE_STOP_TIMEOUT_MS = 5_000L
    /** 첫 화면 목록은 지금 기준 3개까지, 나머지는 "전체 보기". (UX 가이드 1-5) */
    const val MAX_ROWS = 3
    /** 기록 탭의 최근 기록·관찰 개수. 대시보드의 최근 성적도 같은 수. */
    const val MAX_RECENT_RECORDS = 5
    /** 대시보드에 펼치는 메모·피드백 개수. */
    const val MAX_NOTES = 10
    /** 멘토 대시보드의 분석 카드 개수. 학부모 첫 화면은 MAX_ROWS. */
    const val MAX_INSIGHTS = 4
}
