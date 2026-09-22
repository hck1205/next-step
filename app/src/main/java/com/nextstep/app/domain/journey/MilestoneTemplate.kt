package com.nextstep.app.domain.journey

import com.nextstep.app.domain.growth.GrowthStage

/**
 * 나이대별로 "해야 할 것·준비할 것" 하나. 카탈로그에 정적으로 정의되고, 사용자의 완료·건너뛰기 상태만 저장됩니다.
 */
data class MilestoneTemplate(
    /** 안정적인 식별자. 저장된 상태와 연결되므로 바꾸지 않습니다. */
    val id: String,
    val title: String,
    val description: String,
    /** 왜 이 시기에 해야 하는지. 사용자가 판단할 근거. */
    val why: String,
    val category: MilestoneCategory,
    val stage: GrowthStage,
    val due: DueRule,
    /** 마감 몇 개월 전부터 "지금 준비할 것" 에 올릴지. 대기·접수처럼 일찍 움직여야 하는 항목은 크게. */
    val leadMonths: Int,
    /** 1 = 놓치면 되돌리기 어려움, 2 = 중요, 3 = 권장. */
    val priority: Int = 2,
)
