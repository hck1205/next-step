package com.nextstep.app.domain.health

import com.nextstep.app.domain.text.compact
import com.nextstep.app.data.local.entity.GrowthRecordEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 성장 기록(키·몸무게·시력)을 요약하고 참고 신호를 냅니다.
 * 백분위표를 쓰지 않습니다(또래 비교 금지). 대신 아이의 지난 기록과 비교해 "변화"를 보여 주고,
 * 시력 저하나 성장 정체처럼 검진에서 확인할 만한 것만 신호로 냅니다. 의학적 판단은 검진에 맡깁니다.
 */
object GrowthStats {
    /** 시력 검진 권고 기준(학교 검진 재검 기준과 같은 0.7 이하). */
    const val VISION_CHECK_THRESHOLD = 0.7
    /** 최근 기록 대비 이만큼 떨어지면 신호. */
    const val VISION_DROP_THRESHOLD = 0.3
    /** 학령기 전후 연간 키 성장이 이 아래면 검진에서 확인. (일반적으로 만 4세~사춘기 전 연 5~6cm 성장) */
    const val SLOW_HEIGHT_CM_PER_YEAR = 4.0
    private const val MIN_DAYS_FOR_VELOCITY = 60L
    private const val DAYS_PER_YEAR = 365.25

    fun summarize(records: List<GrowthRecordEntity>, today: LocalDate): GrowthSummary {
        val live = records.filter { !it.deleted && !it.isEmpty }.sortedBy { it.date }
        if (live.isEmpty()) return GrowthSummary(null, null, null, null, null, null, null, null, emptyList())
        val latest = live.last()
        val heights = live.filter { it.heightCm != null }
        val weights = live.filter { it.weightKg != null }
        val height = heights.lastOrNull()?.heightCm
        val weight = weights.lastOrNull()?.weightKg
        val visionLeft = live.lastOrNull { it.visionLeft != null }?.visionLeft
        val visionRight = live.lastOrNull { it.visionRight != null }?.visionRight
        val velocity = velocity(heights)
        val weightDelta = weights.takeLast(2).takeIf { it.size == 2 }?.let { (a, b) -> b.weightKg!! - a.weightKg!! }
        val bmi = if (height != null && weight != null && height > 0) weight / ((height / 100) * (height / 100)) else null
        return GrowthSummary(
            latestDate = LocalDate.ofEpochDay(latest.date), heightCm = height, weightKg = weight, visionLeft = visionLeft, visionRight = visionRight,
            heightVelocityCmPerYear = velocity, weightDeltaKg = weightDelta, bmi = bmi,
            signals = signals(live, velocity, today),
        )
    }

    /** 최근 두 키 기록 사이의 연환산 성장 속도. 두 기록이 60일 미만 떨어져 있으면 노이즈라 null. */
    fun velocity(heights: List<GrowthRecordEntity>): Double? {
        val pair = heights.takeLast(2).takeIf { it.size == 2 } ?: return null
        val (a, b) = pair
        val days = b.date - a.date
        if (days < MIN_DAYS_FOR_VELOCITY) return null
        return (b.heightCm!! - a.heightCm!!) / days * DAYS_PER_YEAR
    }

    private fun signals(live: List<GrowthRecordEntity>, velocity: Double?, today: LocalDate): List<GrowthSignal> {
        val out = mutableListOf<GrowthSignal>()
        val visions = live.filter { it.visionLeft != null || it.visionRight != null }
        val latestVision = visions.lastOrNull()
        if (latestVision != null) {
            val worst = listOfNotNull(latestVision.visionLeft, latestVision.visionRight).minOrNull()!!
            val previous = visions.dropLast(1).lastOrNull()
            val prevWorst = previous?.let { listOfNotNull(it.visionLeft, it.visionRight).minOrNull() }
            when {
                worst <= VISION_CHECK_THRESHOLD -> out += GrowthSignal("시력 검진을 권해요", "가장 낮은 쪽 시력이 ${fmt(worst)}이에요. 학교 검진 재검 기준(0.7 이하)이라 안과에서 확인해 보세요. 칠판·책 거리도 함께 살펴보세요.", GrowthSignalLevel.CHECK)
                prevWorst != null && roundTenth(prevWorst - worst) >= VISION_DROP_THRESHOLD -> out += GrowthSignal("시력이 빠르게 떨어졌어요", "지난 기록 ${fmt(prevWorst)} → ${fmt(worst)}. 화면 시간과 야외 활동 시간을 점검하고 안과 검진을 잡아 보세요.", GrowthSignalLevel.CHECK)
            }
        }
        if (velocity != null && velocity < SLOW_HEIGHT_CM_PER_YEAR && velocity >= 0) {
            out += GrowthSignal("키 성장이 느린 편이에요", "최근 연환산 ${fmt(velocity)}cm/년. 사춘기 전엔 보통 연 5~6cm 자라요. 다음 소아과 검진에서 성장 곡선을 함께 봐 달라고 하세요.", GrowthSignalLevel.CHECK)
        }
        val lastDate = LocalDate.ofEpochDay(live.last().date)
        if (ChronoUnit.MONTHS.between(lastDate, today) >= 6) {
            out += GrowthSignal("기록한 지 6개월이 넘었어요", "6개월에 한 번 키·몸무게·시력을 재 두면 변화가 보여요.", GrowthSignalLevel.INFO)
        }
        return out
    }

    /** 시력은 소수 첫째 자리 값이라 차이도 첫째 자리로 반올림해 부동소수점 오차(1.2-0.9=0.2999…)를 없앱니다. */
    private fun roundTenth(v: Double): Double = Math.round(v * 10) / 10.0

    private fun fmt(v: Double): String = v.compact()
}
