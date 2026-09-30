package com.nextstep.app.domain.growth

/**
 * 그 해에 하는 공부 한 가지와 권장 양. 예: 받아쓰기 연습 · 주 2회 · 10분.
 * [minutes] 는 한 번 할 때의 분량이며, 학생이 누르면 이 분량의 오늘 할 일이 됩니다.
 */
data class StudyKind(
    val name: String,
    val type: StudyKindType,
    val timesPerWeek: Int,
    val minutes: Int,
) {
    /** "주 2회 · 10분", 매일이면 "매일 · 10분". */
    val amountLabel: String get() = (if (timesPerWeek >= DAILY) "매일" else "주 ${timesPerWeek}회") + " · ${minutes}분"

    private companion object {
        const val DAILY = 7
    }
}
