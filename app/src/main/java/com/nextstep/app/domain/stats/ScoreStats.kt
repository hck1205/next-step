package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.GradeEntity

/** 과목 평균 점수들의 전체 평균. 점수가 없으면 null. */
object ScoreStats {
    /** 성적 기록들의 백분율 평균. 기록이 없으면 null. */
    fun averagePercent(grades: List<GradeEntity>): Double? = grades.takeIf { it.isNotEmpty() }?.map { it.percent }?.average()

    fun overallAverage(scores: List<SubjectScore>): Double? = scores.takeIf { it.isNotEmpty() }?.map { it.average }?.average()
}
