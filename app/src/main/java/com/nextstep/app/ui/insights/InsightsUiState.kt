package com.nextstep.app.ui.insights

import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.insight.Insight
import com.nextstep.app.domain.insight.Talent
import com.nextstep.app.domain.stats.DayMinutes
import com.nextstep.app.domain.stats.SubjectMinutes
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.stats.SubjectScore

data class InsightsUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
    val daily14: List<DayMinutes> = emptyList(),
    val weeklyBySubject: List<SubjectMinutes> = emptyList(),
    val byHour: IntArray = IntArray(24),
    val notes: List<NoteEntity> = emptyList(),
    val totalMinutes: Int = 0,
    val talents: List<Talent> = emptyList(),
)
