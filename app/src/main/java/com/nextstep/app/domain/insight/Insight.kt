package com.nextstep.app.domain.insight

data class Insight(
    val kind: InsightKind,
    val title: String,
    val body: String,
    val subjectId: String? = null,
    /** 실행 제안이 있으면 버튼으로 노출. */
    val action: InsightAction? = null,
)
