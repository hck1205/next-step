package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.GradeEntity

/** 성적 평균. */
object ScoreStats {
    /** 성적 기록들의 백분율 평균. 기록이 없으면 null. */
    fun averagePercent(grades: List<GradeEntity>): Double? = grades.takeIf { it.isNotEmpty() }?.map { it.percent }?.average()
}
