package com.nextstep.app.domain.insight

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.data.model.AptitudeDomain
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 활동 기록 + 관찰 메모로 예체능·비교과 소질 신호를 찾습니다.
 * 근거는 네 가지뿐입니다: (1) 어른의 관찰(강도 1~3), (2) 그 영역 활동을 한 횟수, (3) 취미·동아리를 이어 온 개월 수,
 * (4) 아이가 별점 4 이상을 준 활동. 점수는 순위와 다음 한 걸음을 정하는 데만 쓰고, 화면은 근거 문장을 보여 줍니다.
 * 키·몸무게 같은 신체 수치는 소질 판단에 쓰지 않습니다(성장은 성장 기록에서 따로 봅니다).
 */
object AptitudeEngine {
    private const val OBSERVATION_WEIGHT = 2
    private const val HIGH_RATING = 4
    private const val MONTHS_PER_POINT = 3
    private const val MIN_SCORE = 3
    private const val STAGE_2_SCORE = 6
    private const val STAGE_3_SCORE = 12
    private const val MAX_SIGNALS = 3

    fun signals(activities: List<ActivityEntity>, observations: List<ObservationEntity>, today: LocalDate): List<AptitudeSignal> {
        val live = activities.filter { !it.deleted }
        val obs = observations.filter { !it.deleted }
        return AptitudeDomain.entries.mapNotNull { domain -> signalFor(domain, live, obs, today) }
            .filter { it.score >= MIN_SCORE }
            .sortedByDescending { it.score }
            .take(MAX_SIGNALS)
    }

    /** 활동이 속한 영역. 종류가 운동·무용이면 제목 없이도 정해지고, 나머지는 제목 키워드로. */
    fun domainOf(activity: ActivityEntity): AptitudeDomain? = AptitudeDomain.guessFrom(activity.title) ?: when (activity.type) {
        ActivityType.VOLUNTEER -> AptitudeDomain.SOCIAL
        else -> null
    }

    private fun signalFor(domain: AptitudeDomain, activities: List<ActivityEntity>, observations: List<ObservationEntity>, today: LocalDate): AptitudeSignal? {
        val mine = activities.filter { domainOf(it) == domain }
        val myObs = observations.filter { it.domain == domain }
        if (mine.isEmpty() && myObs.isEmpty()) return null
        val evidence = mutableListOf<String>()
        var score = 0

        val obsScore = myObs.sumOf { it.strength } * OBSERVATION_WEIGHT
        if (myObs.isNotEmpty()) { score += obsScore; evidence += "어른의 관찰 ${myObs.size}회" + (myObs.maxOf { it.strength }.takeIf { it >= 3 }?.let { " · 남들이 먼저 알아봄" } ?: "") }

        if (mine.isNotEmpty()) { score += mine.size; evidence += "${domain.label} 활동 ${mine.size}개" }

        val ongoing = mine.filter { it.isOngoing }
        val months = ongoing.maxOfOrNull { ChronoUnit.MONTHS.between(LocalDate.ofEpochDay(it.date), today).toInt().coerceAtLeast(0) } ?: 0
        if (months > 0) { score += months / MONTHS_PER_POINT; evidence += "${ongoing.first().title} ${months}개월째 이어 감" }

        val loved = mine.count { it.rating >= HIGH_RATING }
        if (loved > 0) { score += loved; evidence += "아이가 별점 ${HIGH_RATING} 이상 준 활동 ${loved}개" }

        val stage = when { score >= STAGE_3_SCORE -> 3; score >= STAGE_2_SCORE -> 2; else -> 1 }
        return AptitudeSignal(domain, score, evidence, stage, nextStep(domain, stage))
    }

    private fun nextStep(domain: AptitudeDomain, stage: Int): String = when (stage) {
        1 -> "${domain.label} 쪽 체험을 한 번 더 해 보고 아이 반응을 관찰 메모로 남겨 보세요"
        2 -> "주 1회 정기 활동(학원·동아리·클럽)으로 이어 가고 3개월 뒤 다시 보세요"
        else -> "발표·대회·전시 같은 무대에 한 번 서 보게 하고, 전문가(선생님·코치)의 의견을 들어 보세요"
    }
}
