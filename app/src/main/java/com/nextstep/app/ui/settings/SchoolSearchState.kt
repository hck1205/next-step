package com.nextstep.app.ui.settings

import com.nextstep.app.domain.school.School

/** "학교" 창의 찾기 결과와 안내 한 줄(받아 온 일정 수 · 오류). [busy] 면 찾거나 받는 중. */
data class SchoolSearchState(
    val results: List<School> = emptyList(),
    val message: String? = null,
    val busy: Boolean = false,
)
