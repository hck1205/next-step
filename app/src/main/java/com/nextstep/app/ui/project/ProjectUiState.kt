package com.nextstep.app.ui.project

import com.nextstep.app.domain.project.PhaseSlot
import com.nextstep.app.domain.project.ProjectProgress
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/** 교육 프로젝트 한 개의 화면. [progress] 가 null 이면 지워졌거나 카탈로그에 없는 프로젝트입니다. */
data class ProjectUiState(
    val loaded: Boolean = false,
    val progress: ProjectProgress? = null,
    val slots: List<PhaseSlot> = emptyList(),
    /** 이미 할 수 있어 건너뛴 앞 단계 수. */
    val skipped: Int = 0,
    val today: LocalDate = DateUtils.today(),
)
