package com.nextstep.app.ui.progress

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.stats.SubjectProgress

data class ProgressUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
)
