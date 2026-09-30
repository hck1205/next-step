package com.nextstep.app.domain.journey

/**
 * 장기 목표 하나를 구간(학기)별 단계로 미리 쪼개 둔 트랙. 사용자가 시작하면 목표와 단계가 저장소에 복사됩니다.
 * 단계의 [TrackStep.periodKey] 는 [JourneyPeriod.key] 형식이며, 아이의 달력에 없는 구간(입학 시기 차이)은 건너뜁니다.
 */
data class GoalTrack(
    /** 안정적인 식별자. 저장된 목표와 연결되므로 바꾸지 않습니다. */
    val id: String,
    val title: String,
    val area: GoalArea,
    val description: String,
    val steps: List<TrackStep>,
) {
    val firstPeriodKey: String get() = steps.first().periodKey
    val lastPeriodKey: String get() = steps.last().periodKey
}
