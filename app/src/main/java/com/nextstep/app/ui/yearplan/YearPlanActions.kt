package com.nextstep.app.ui.yearplan

/** "올해" 탭 밖으로: 여정(여러 해 타임라인)으로. 어린 단계는 null 이라 버튼이 없습니다. */
data class YearPlanActions(
    val onOpenJourney: (() -> Unit)? = null,
)
