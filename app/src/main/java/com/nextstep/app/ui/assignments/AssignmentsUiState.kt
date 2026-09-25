package com.nextstep.app.ui.assignments

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.mentor.AssignmentReport
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/** 과제·피드백 › 과제: 멘토가 낸 과제의 완료율·밀린 것·곧 마감·과목별. */
data class AssignmentsUiState(
    val report: AssignmentReport? = null,
    val subjects: List<SubjectEntity> = emptyList(),
    val today: LocalDate = DateUtils.today(),
    val loaded: Boolean = false,
)
