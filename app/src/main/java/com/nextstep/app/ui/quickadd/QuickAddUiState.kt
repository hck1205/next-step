package com.nextstep.app.ui.quickadd

import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.data.local.entity.SubjectEntity
import java.time.LocalDate

data class QuickAddUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val today: LocalDate = DateUtils.today(),
    /** 저장 직후 한 줄 토스트. 화면이 보여 준 뒤 지웁니다. */
    val savedMessage: String? = null,
)
