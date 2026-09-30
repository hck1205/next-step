package com.nextstep.app.ui.insights

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.insight.Insight
import com.nextstep.app.domain.insight.Talent
import com.nextstep.app.domain.stats.DayMinutes
import com.nextstep.app.domain.stats.SubjectMinutes
import com.nextstep.app.domain.stats.SubjectScore

data class InsightsUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    /** 레이더에 겹쳐 그릴 복습 완료율. [scores] 와 같은 과목 순서. */
    val reviewRatios: List<Float> = emptyList(),
    val daily14: List<DayMinutes> = emptyList(),
    val weeklyBySubject: List<SubjectMinutes> = emptyList(),
    val byHour: IntArray = IntArray(24),
    val totalMinutes: Int = 0,
    val talents: List<Talent> = emptyList(),
)
