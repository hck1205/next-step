package com.nextstep.app.ui.yearplan

/**
 * "올해" 화면 밖으로: 여정(여러 해 타임라인)으로. 어린 단계는 null 이라 버튼이 없습니다.
 * [onBack] 은 학부모·멘토가 여정에서 열었을 때만(학생은 하단 탭이라 null).
 */
data class YearPlanActions(
    val onOpenJourney: (() -> Unit)? = null,
    val onBack: (() -> Unit)? = null,
)
