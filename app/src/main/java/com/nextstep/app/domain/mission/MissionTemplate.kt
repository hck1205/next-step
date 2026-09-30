package com.nextstep.app.domain.mission

import com.nextstep.app.domain.journey.GoalArea

/** 목표 종류 하나의 단계 설계. 단계는 [MissionStepTemplate.daysBefore] 가 큰 것부터(먼저 할 것부터) 둡니다. */
data class MissionTemplate(val kind: MissionKind, val area: GoalArea, val steps: List<MissionStepTemplate>) {
    /** 준비 기간: 가장 이른 단계가 목표 날짜 며칠 전인지. */
    val spanDays: Int get() = steps.maxOfOrNull { it.daysBefore }?.coerceAtLeast(0) ?: 0

    fun titleFor(subject: String?): String = listOfNotNull(subject?.trim()?.takeIf { it.isNotEmpty() }, kind.label).joinToString(" ")
}
