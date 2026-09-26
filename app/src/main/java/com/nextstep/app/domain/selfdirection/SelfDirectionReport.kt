package com.nextstep.app.domain.selfdirection

/**
 * 기록 › 공부 › 스스로 화면의 요약: 지금 단계, 학부모가 직접 고른 단계인지, 최근 4주 흔적, 스스로 만든 할 일 비율, 제안.
 */
data class SelfDirectionReport(
    val stage: SelfDirectionStage,
    val defaultStage: SelfDirectionStage,
    val weeks: List<WeekEvidence>,
    val selfTaskRatio: Float?,
    val suggestion: StageSuggestion?,
) {
    val chosen: Boolean get() = stage != defaultStage
    val childPlannedWeeks: Int get() = weeks.count { it.childPlanned }
    val reflectedWeeks: Int get() = weeks.count { it.reflected }
}
