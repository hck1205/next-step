package com.nextstep.app.domain.familytalk

/**
 * 한 주의 반짝인 순간(좋았던 것만 — 밀린 것·점수 하락·비교는 모으지 않음).
 * [sparkles] 는 주간 피드백 가운데 잘한 사실을 아이의 말로 옮긴 것.
 */
data class WeekHighlights(
    val doneTasks: Int = 0,
    val studyDays: Int = 0,
    val cheers: Int = 0,
    val goalsDone: List<String> = emptyList(),
    val sparkles: List<String> = emptyList(),
) {
    val isEmpty: Boolean get() = doneTasks == 0 && studyDays == 0 && cheers == 0 && goalsDone.isEmpty() && sparkles.isEmpty()
}
