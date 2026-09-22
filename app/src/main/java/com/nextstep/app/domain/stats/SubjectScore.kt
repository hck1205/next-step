package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.SubjectEntity

/** 과목별 성적 요약. */
data class SubjectScore(
    val subject: SubjectEntity,
    val average: Double,
    val latest: Double?,
    /** 최근 두 시험의 점수 차. 양수면 상승. */
    val trend: Double?,
    val count: Int,
    /** 반 평균 대비 차이(가장 최근 시험). */
    val vsClass: Double?,
)
