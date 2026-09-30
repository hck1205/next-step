package com.nextstep.app.domain.project

/** 계획한 날짜와 비교한 진행 상태. 느려도 괜찮다는 말을 함께 보여 줍니다(비교 대상은 계획뿐). */
enum class ProjectPace(val label: String) {
    AHEAD("계획보다 빨라요"),
    ON_TRACK("계획대로예요"),
    BEHIND("계획보다 늦어요"),
    DONE("목표에 닿았어요"),
}
