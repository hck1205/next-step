package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.SubjectEntity

/** 한 과목의 점수 흐름(오래된 시험부터, 100점 만점으로 환산). [classAverage] 는 반 평균이 적힌 시험들의 평균. */
data class ScoreSeries(val subject: SubjectEntity, val percents: List<Int>, val classAverage: Int?) {
    val last: Int? get() = percents.lastOrNull()
    /** 직전 시험보다 오른(+)·내린(-) 점수. 시험이 하나면 null. */
    val change: Int? get() = if (percents.size < 2) null else percents[percents.size - 1] - percents[percents.size - 2]
}
