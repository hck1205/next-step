package com.nextstep.app.ui.goals

/** Goals 화면 밖으로 나가는 내비게이션 콜백. 기본값은 no-op 이라 프리뷰·테스트에서 생략할 수 있습니다. */
data class GoalsActions(
    /** null 이면 기록 탭 안의 섹션으로 그려져 제목줄이 없습니다. */
    val onBack: (() -> Unit)? = null,
    val onOpenJourney: () -> Unit = {},
)
