package com.nextstep.app.domain.stats

/** 과목 평균 점수들의 전체 평균. 점수가 없으면 null. */
object ScoreStats {
    fun overallAverage(scores: List<SubjectScore>): Double? = scores.takeIf { it.isNotEmpty() }?.map { it.average }?.average()
}
