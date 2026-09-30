package com.nextstep.app.ui.timer

import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.prefs.RunningTimer

data class TimerUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val running: RunningTimer? = null,
    val elapsedSeconds: Long = 0,
    val todayMinutes: Int = 0,
    val todaySessions: List<StudySessionEntity> = emptyList(),
    val selectedSubjectId: String? = null,
    val lastSaved: StudySessionEntity? = null,
)
