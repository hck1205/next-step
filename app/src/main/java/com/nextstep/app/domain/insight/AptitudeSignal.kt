package com.nextstep.app.domain.insight

import com.nextstep.app.data.model.AptitudeDomain

/** 예체능·비교과 소질 신호 하나. 근거와 다음 한 걸음을 함께 냅니다. */
data class AptitudeSignal(
    val domain: AptitudeDomain,
    /** 근거 강도의 합. 화면은 순위와 단계 판단에만 씁니다. */
    val score: Int,
    val evidence: List<String>,
    /** 1 = 체험 더, 2 = 정기 활동, 3 = 무대·대회 도전. */
    val stage: Int,
    val nextStep: String,
)
