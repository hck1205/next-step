package com.nextstep.app.domain.growth

import com.nextstep.app.domain.planner.PlanOptions

/**
 * 단계별 지침. 규칙 엔진과 화면이 그대로 보여주는 텍스트라 근거(왜 이 시기에 이것이 중요한지)를 함께 담습니다.
 */
data class StageGuide(
    val stage: GrowthStage,
    /** 이 시기의 "양질의 입력"이 무엇인지 한 문단. */
    val inputPrinciple: String,
    /** 학부모가 지금 해 줄 일. */
    val parentTips: List<String>,
    /** 멘토가 큐레이팅·지도할 때의 기준. */
    val mentorTips: List<String>,
    /** 공부 밖에서 쌓아야 할 경험 제안. */
    val experiences: List<String>,
    /** 칭찬의 방향(부모 가이드). */
    val praiseStyle: String,
    /** 학습 계획 기본값. */
    val planOptions: PlanOptions,
)
