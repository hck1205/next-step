package com.nextstep.app.domain.health

import java.time.LocalDate

/** 성장 기록 요약. 비교 대상은 또래가 아니라 지난 기록입니다. */
data class GrowthSummary(
    val latestDate: LocalDate?,
    val heightCm: Double?,
    val weightKg: Double?,
    val visionLeft: Double?,
    val visionRight: Double?,
    /** 최근 두 기록 사이의 키 변화를 연 단위로 환산(cm/년). 기록이 하나뿐이면 null. */
    val heightVelocityCmPerYear: Double?,
    /** 최근 두 기록 사이의 몸무게 변화(kg). */
    val weightDeltaKg: Double?,
    val bmi: Double?,
    val signals: List<GrowthSignal>,
) {
    val hasAny: Boolean get() = latestDate != null
}
